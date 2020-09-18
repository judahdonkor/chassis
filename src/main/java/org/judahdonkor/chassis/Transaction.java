package org.judahdonkor.chassis;

import java.util.function.Supplier;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

@FunctionalInterface
public interface Transaction {
	@FunctionalInterface
	public static interface Executor {
		void run();
	}

	default void execute(Executor executor) {
		this.<Void>execute(() -> {
			executor.run();
			return null;
		});
	};

	<T> T execute(Supplier<T> supplier);

	public static Transaction of(UserTransaction ut) {
		return new Transaction() {
			@Override
			public <T> T execute(Supplier<T> supplier) {
				var manage = false;
				T rtn = null;
				try {
					manage = ut.getStatus() == Status.STATUS_NO_TRANSACTION;
					if (manage)
						ut.begin();
					rtn = supplier.get();
					if (manage)
						ut.commit();
				} catch (Exception e) {
					if (manage)
						try {
							ut.rollback();
						} catch (Exception se) {
							se.initCause(e);
							throw Beef.uncheck(se);
						}
					throw Beef.uncheck(e);
				}
				return rtn;
			}
		};
	}
}
