package com.eiff.framework.common.biz.pkg.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class Sort implements Iterable<com.eiff.framework.common.biz.pkg.page.Sort.Order>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final Direction DEFAULT_DIRECTION = Direction.ASC;

	private List<Order> orders = new ArrayList<>();

	public Sort() {
	}

	public Sort(String... properties) {
		this(DEFAULT_DIRECTION, properties);
	}

	public Sort(Direction direction, String... properties) {
		this(direction, properties == null ? new ArrayList<String>() : Arrays.asList(properties));
	}

	private Sort(Direction direction, List<String> properties) {

		if (properties == null || properties.isEmpty()) {
			throw new IllegalArgumentException("You have to provide at least one property to sort by!");
		}

		for (String property : properties) {
			this.orders.add(new Order(direction, property));
		}
	}

	public Iterator<Order> iterator() {
		return this.orders.iterator();
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public List<Order> getOrders() {
		return orders;
	}

	@Override
	public String toString() {
		return StringUtils.join(orders, " and ");
	}

	public static enum Direction {

		ASC, DESC;

		public boolean isAscending() {
			return this.equals(ASC);
		}

		public boolean isDescending() {
			return this.equals(DESC);
		}

		public static Direction fromString(String value) {

			try {
				return Direction.valueOf(value.toUpperCase(Locale.US));
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format(
						"Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).",
						value), e);
			}
		}

		public static Direction fromStringOrNull(String value) {

			try {
				return fromString(value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	public static class Order implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private static final boolean DEFAULT_IGNORE_CASE = false;

		private Direction direction;
		private String property;
		private boolean ignoreCase;

		public Order(Direction direction, String property) {
			this(direction, property, DEFAULT_IGNORE_CASE);
		}

		private Order(Direction direction, String property, boolean ignoreCase) {

			if (StringUtils.isBlank(property)) {
				throw new IllegalArgumentException("Property must not null or empty!");
			}

			this.direction = direction == null ? DEFAULT_DIRECTION : direction;
			this.property = property;
			this.ignoreCase = ignoreCase;
		}

		public Direction getDirection() {
			return direction;
		}

		public String getProperty() {
			return property;
		}

		public boolean isAscending() {
			return this.direction.isAscending();
		}

		public boolean isDescending() {
			return this.direction.isDescending();
		}

		public boolean isIgnoreCase() {
			return ignoreCase;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		@Override
		public String toString() {
			return this.property + " " + this.direction;
		}
	}
}
