package org.judahdonkor.chassis;

import javax.ws.rs.core.Response;

import org.judahdonkor.chassis.Beef.UncheckedException;

public class UserException extends UncheckedException {
    private static final long serialVersionUID = 7918826341720024388L;

    @Override
    public Response.Status httpStatus() {
        return Response.Status.UNAUTHORIZED;
    }

    public UserException(org.judahdonkor.chassis.Error.Builder error, Throwable cause) {
        super(error, cause);
    }

    public UserException(org.judahdonkor.chassis.Error.Builder error) {
        super(error);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException() {
    }
}