package com.taypo.microplot;

import java.time.LocalDateTime;

public class Measurement {
	public LocalDateTime x;
	public double y;

	public Measurement(LocalDateTime x, double y) {
		this.x = x;
		this.y = y;
	}
}
