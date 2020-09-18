package org.judahdonkor.chassis;

import javax.ws.rs.core.Response;

import org.judahdonkor.chassis.Beef.UncheckedException;

public class ValidationException extends UncheckedException {
    private static final long serialVersionUID = 7653984050828149589L;

    @Override
    public Response.Status httpStatus() {
        return Response.Status.BAD_REQUEST;
    }

    public ValidationException(org.judahdonkor.chassis.Error.Builder error, Throwable cause) {
        super(error, cause);
    }

    public ValidationException(org.judahdonkor.chassis.Error.Builder error) {
        super(error);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException() {
    }
}