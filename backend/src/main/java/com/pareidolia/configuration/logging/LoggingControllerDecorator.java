package com.pareidolia.configuration.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingControllerDecorator {
	@Around("execution(* com.pareidolia.controller..*.*(..))")
	public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();

		// Log prima dell'esecuzione del metodo
		log.info("→ Executing {}.{}() with arguments: {}",
			className,
			methodName,
			Arrays.toString(joinPoint.getArgs())
		);

		long startTime = System.currentTimeMillis();

		try {
			// Esegue il metodo
			Object result = joinPoint.proceed();

			// Log dopo l'esecuzione con successo
			long duration = System.currentTimeMillis() - startTime;
			log.info("← {}.{}() completed in {}ms with result: {}",
				className,
				methodName,
				duration,
				result != null ? result.toString() : "void"
			);

			return result;

		} catch (Exception e) {
			// Log in caso di errore
			long duration = System.currentTimeMillis() - startTime;
			log.error("× {}.{}() failed after {}ms with exception: {}",
				className,
				methodName,
				duration,
				e.getMessage()
			);
			throw e;
		}
	}
}