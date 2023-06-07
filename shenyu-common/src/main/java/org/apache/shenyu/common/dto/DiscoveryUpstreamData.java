package org.apache.shenyu.common.dto;

public class DiscoveryUpstreamData {

    private String id;

    /**
     * discovery id.
     */
    private String discoveryId;

    /**
     * protocol.
     */
    private String protocol;

    /**
     * url.
     */
    private String url;

    /**
     * status.
     */
    private Integer status;

    /**
     * weight.
     */
    private Integer weight;

    /**
     * props.
     */
    private String props;

    /**
     * getId.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * setId.
     *
     * @param id id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * getDiscoveryId.
     *
     * @return discoveryId
     */
    public String getDiscoveryId() {
        return discoveryId;
    }

    /**
     * setDiscoveryId.
     *
     * @param discoveryId discoveryId
     */
    public void setDiscoveryId(final String discoveryId) {
        this.discoveryId = discoveryId;
    }

    /**
     * getProtocol.
     *
     * @return protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * setProtocol.
     *
     * @param protocol protocol
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
     * getUrl.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * setUrl.
     *
     * @param url url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * getStatus.
     *
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * setStatus。
     *
     * @param status status
     */
    public void setStatus(final Integer status) {
        this.status = status;
    }

    /**
     * getWeight.
     *
     * @return weight
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * setWeight.
     *
     * @param weight weight
     */
    public void setWeight(final Integer weight) {
        this.weight = weight;
    }

    /**
     * getProps.
     *
     * @return props
     */
    public String getProps() {
        return props;
    }

    /**
     * setProps.
     *
     * @param props props
     */
    public void setProps(final String props) {
        this.props = props;
    }
}
