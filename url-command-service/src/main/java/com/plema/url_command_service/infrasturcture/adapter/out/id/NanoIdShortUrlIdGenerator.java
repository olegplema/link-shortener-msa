package com.plema.url_command_service.infrasturcture.adapter.out.id;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.plema.url_command_service.application.port.out.ShortUrlIdGenerator;
import org.springframework.stereotype.Component;

@Component
public class NanoIdShortUrlIdGenerator implements ShortUrlIdGenerator {

    @Override
    public String nextId() {
        return NanoIdUtils.randomNanoId();
    }
}
