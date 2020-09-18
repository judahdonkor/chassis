package org.judahdonkor.chassis;

import org.judahdonkor.chassis.Beef.UncheckedException;

public class ConfigException extends UncheckedException {
    private static final long serialVersionUID = -6547172699595389978L;

    public ConfigException(org.judahdonkor.chassis.Error.Builder error, Throwable cause) {
        super(error, cause);
    }

    public ConfigException(org.judahdonkor.chassis.Error.Builder error) {
        super(error);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

    public ConfigException() {
    }
}