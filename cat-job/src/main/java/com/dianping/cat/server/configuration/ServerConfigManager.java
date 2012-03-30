package com.dianping.cat.server.configuration;

import java.io.File;

import com.dianping.cat.server.configuration.entity.ServerConfig;
import com.dianping.cat.server.configuration.transform.DefaultXmlParser;
import com.site.helper.Files;

public class ServerConfigManager {
	private ServerConfig m_config;

	public void initialize(File configFile) throws Exception {
		String xml = Files.forIO().readFrom(configFile, "utf-8");
		ServerConfig config = new DefaultXmlParser().parse(xml);

		// do validation
		config.accept(new ServerConfigValidator());

		m_config = config;
	}

	public ServerConfig getServerConfig() {
		return m_config;
	}
}
