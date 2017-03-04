package no.mesan.handterminator.model.maps;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author Sondre Sparby Boge
 * Simple storage format for distance and duration in a Leg.
 */

public class TextValue implements Serializable {
	private String text;
	private long value;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("text", text)
				.append("value", value)
				.toString();
	}
}
