package com.ylivirta.util;

public class Any<T>
{
	private final T value;

	public Any(T value) {
		this.value = value;
	}

	public double asDouble() {
		return (Double) this.value;
	}

	public float asFloat() {
		return (Float) this.value;
	}

	public int asInteger() {
		return (Integer) this.value;
	}

	public String asString() {
		return (String) this.value;
	}
}
