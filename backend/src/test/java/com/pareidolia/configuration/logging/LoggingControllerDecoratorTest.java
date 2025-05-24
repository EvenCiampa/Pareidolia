package com.pareidolia.configuration.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoggingControllerDecoratorTest {

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private MethodSignature methodSignature;

	@InjectMocks
	private LoggingControllerDecorator loggingDecorator;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getDeclaringType()).thenReturn(TestController.class);
		when(methodSignature.getName()).thenReturn("testMethod");
		when(joinPoint.getArgs()).thenReturn(new Object[]{"testArg"});
	}

	@Test
	void testLogControllerMethods_Success() throws Throwable {
		// Setup
		String expectedResult = "success";
		when(joinPoint.proceed()).thenReturn(expectedResult);

		// Test
		Object result = loggingDecorator.logControllerMethods(joinPoint);

		// Verify
		assertEquals(expectedResult, result);
		verify(joinPoint).proceed();
	}

	@Test
	void testLogControllerMethods_NullResult() throws Throwable {
		// Setup
		when(joinPoint.proceed()).thenReturn(null);

		// Test
		Object result = loggingDecorator.logControllerMethods(joinPoint);

		// Verify
		assertNull(result);
		verify(joinPoint).proceed();
	}

	@Test
	void testLogControllerMethods_Exception() throws Throwable {
		// Setup
		RuntimeException expectedException = new RuntimeException("Test exception");
		when(joinPoint.proceed()).thenThrow(expectedException);

		// Test
		Exception actualException = assertThrows(RuntimeException.class,
			() -> loggingDecorator.logControllerMethods(joinPoint));

		// Verify
		assertEquals(expectedException, actualException);
		verify(joinPoint).proceed();
	}

	// Test controller class for mocking
	private static class TestController {
		public String testMethod(String arg) {
			return "test";
		}
	}
} 