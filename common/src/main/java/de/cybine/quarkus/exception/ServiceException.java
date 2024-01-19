package de.cybine.quarkus.exception;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.util.*;
import java.util.stream.*;

@Getter
@SuppressWarnings("unused")
public class ServiceException extends RuntimeException
{
    protected final String code;

    protected final int statusCode;

    @Getter(AccessLevel.NONE)
    protected final List<BiTuple<String, Object>> data = new ArrayList<>();

    public ServiceException(String code, int statusCode, String message)
    {
        this(code, statusCode, message, null);
    }

    public ServiceException(String code, int statusCode, String message, Throwable cause)
    {
        super(message, cause);

        this.code = code;
        this.statusCode = statusCode;
    }

    @SuppressWarnings("unchecked")
    public <T extends ServiceException> T addData(String key, Object value)
    {
        if (key == null || value == null)
            throw new IllegalArgumentException("Key and value must not be null");

        this.data.add(new BiTuple<>(key, value));
        return (T) this;
    }

    public ApiError toResponse( )
    {
        return ApiError.builder()
                       .code(this.code)
                       .message(this.getMessage())
                       .data(this.data.stream().collect(Collectors.toMap(BiTuple::first, BiTuple::second)))
                       .build();
    }
}
