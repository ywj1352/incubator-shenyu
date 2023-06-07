/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.protocol.tcp;

import com.google.common.eventbus.EventBus;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.protocol.tcp.connection.Bridge;
import org.apache.shenyu.protocol.tcp.connection.ConnectionContext;
import org.apache.shenyu.protocol.tcp.connection.DefaultConnectionConfigProvider;
import org.apache.shenyu.protocol.tcp.connection.TcpConnectionBridge;
import org.apache.shenyu.protocol.tcp.connection.ActivityConnectionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

import java.net.SocketAddress;
import java.util.List;


/**
 * BootstrapServer.
 */
public class TcpBootstrapServer implements BootstrapServer {
    private static final Logger LOG = LoggerFactory.getLogger(TcpBootstrapServer.class);

    private Bridge bridge;

    private ConnectionContext connectionContext;

    private DisposableServer server;

    private final EventBus eventBus;

    public TcpBootstrapServer(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void start(final TcpServerConfiguration tcpServerConfiguration) {
        String loadBalanceAlgorithm = tcpServerConfiguration.getProps().getOrDefault("shenyu.tcpPlugin.tcpServerConfiguration.props.loadBalanceAlgorithm", "random").toString();
        DefaultConnectionConfigProvider connectionConfigProvider = new DefaultConnectionConfigProvider(loadBalanceAlgorithm, tcpServerConfiguration.getPluginSelectorName());
        this.bridge = new TcpConnectionBridge();
        connectionContext = new ConnectionContext(connectionConfigProvider);
        connectionContext.init(tcpServerConfiguration.getProps());
        LoopResources loopResources = LoopResources.create("shenyu-tcp-bootstrap-server", tcpServerConfiguration.getBossGroupThreadCount(),
                tcpServerConfiguration.getWorkerGroupThreadCount(), true);

        TcpServer tcpServer = TcpServer.create()
                .doOnChannelInit((connObserver, channel, remoteAddress) -> {
                    channel.pipeline().addFirst(new LoggingHandler(LogLevel.INFO));
                })
                .wiretap(true)
                .observe((c, s) -> {
                    LOG.info("connection={}|status={}", c, s);
                })
                //.childObserve(connectionObserver)
                .doOnConnection(this::bridgeConnections)
                .port(tcpServerConfiguration.getPort())
                .runOn(loopResources);
        server = tcpServer.bindNow();
    }

    private void bridgeConnections(final Connection serverConn) {
        LOG.info("Starting proxy client ={}", serverConn);
        SocketAddress socketAddress = serverConn.channel().remoteAddress();
        ActivityConnectionObserver connectionObserver = new ActivityConnectionObserver("TcpClient");
        eventBus.register(connectionObserver);
        Mono<Connection> client = connectionContext.getTcpClientConnection(getIp(socketAddress), connectionObserver);
        client.subscribe(clientConn -> bridge.bridge(serverConn, clientConn));
    }

    private String getIp(final SocketAddress socketAddress) {
        if (socketAddress == null) {
            throw new NullPointerException("remoteAddress is null");
        }
        String address = socketAddress.toString();
        return address.substring(2, address.indexOf(':'));
    }

    /**
     * doOnUpdate.
     *
     * @param removeList removeList
     */
    @Override
    public void removeCommonUpstream(final List<DiscoveryUpstreamData> removeList) {
        eventBus.post(removeList);
    }


    /**
     * shutdown.
     */
    @Override
    public void shutdown() {
        server.disposeNow();
    }

}
