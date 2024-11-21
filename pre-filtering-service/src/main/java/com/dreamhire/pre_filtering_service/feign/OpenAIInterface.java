package com.dreamhire.pre_filtering_service.feign;

import com.dreamhire.pre_filtering_service.dtos.RequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

@FeignClient("AI-OPEN")
public interface OpenAIInterface {
    @PostMapping("api/getresponse")
    public String getResponseFromOpenAi(@RequestBody RequestDto requestDto) throws IOException;
}
