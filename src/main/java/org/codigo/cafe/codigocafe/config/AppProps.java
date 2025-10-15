package org.codigo.cafe.codigocafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProps {

    @Value("${app.urlApi}")
    private String urlApi;

    public String getUrlApi() {
        return urlApi;
    }

}
