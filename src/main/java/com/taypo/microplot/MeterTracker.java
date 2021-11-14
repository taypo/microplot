package com.taypo.microplot;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO make configurable
public class MeterTracker {
	private Map<String, Collection<Measurement>> store = new HashMap<>();

	private final MeterRegistry meterRegistry;
	private final MPConfig config;

	public MeterTracker(MeterRegistry meterRegistry, MPConfig config) {
		this.meterRegistry = meterRegistry;
		this.config = config;
	}

	@Scheduled(fixedRate = 5000)
	public void tick() {
		for (var meter : config.getIncludeMetrics()) {
			if (store.containsKey(meter) == false) {
				store.put(meter, new CircularFifoQueue<>(200));
			}
			Collection<Measurement> data = store.get(meter);
			Optional<Counter> counter = Optional.ofNullable(meterRegistry.find(meter).counter());
			counter.ifPresent((it) -> data.add(new Measurement(LocalDateTime.now(), it.count())));
		}
	}

	public Map<String, Collection<Measurement>> getStore() {
		return store;
	}
}
