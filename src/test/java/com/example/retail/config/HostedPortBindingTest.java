package com.example.retail.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HostedPortBindingTest {

    @Test
    void localDefault_faraProxy() {
        HostedPortBinding.Ports ports = HostedPortBinding.resolve(null, null, null, "dev");
        assertEquals(8080, ports.publicPort());
        assertEquals(8080, ports.internalPort());
        assertFalse(ports.proxy());
    }

    @Test
    void render_port10000_proxyPeTomcat8080() {
        HostedPortBinding.Ports ports = HostedPortBinding.resolve("10000", "true", null, "prod");
        assertEquals(10000, ports.publicPort());
        assertEquals(8080, ports.internalPort());
        assertTrue(ports.proxy());
    }

    @Test
    void cloudRun_port8080_internal8081() {
        HostedPortBinding.Ports ports = HostedPortBinding.resolve("8080", null, "retail-stock-manager", "prod");
        assertEquals(8080, ports.publicPort());
        assertEquals(8081, ports.internalPort());
        assertTrue(ports.proxy());
    }

    @Test
    void prodCuPort_chiarFaraRenderEnv() {
        HostedPortBinding.Ports ports = HostedPortBinding.resolve("10000", null, null, "prod");
        assertTrue(ports.proxy());
        assertEquals(8080, ports.internalPort());
    }

    @Test
    void dockerCompose_faraPortEnv_faraProxy() {
        HostedPortBinding.Ports ports = HostedPortBinding.resolve(null, null, null, "docker");
        assertFalse(ports.proxy());
        assertEquals(8080, ports.publicPort());
    }
}
