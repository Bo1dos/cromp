package com.cromp.executions.application.port;

import java.util.Optional;

/**
* Получить сериализованный конфиг версии job — нужен для snapshot и для claim result 
*/
public interface JobVersionQueryPort {
    Optional<String> findConfigJsonByVersionId(Long jobVersionId);
}