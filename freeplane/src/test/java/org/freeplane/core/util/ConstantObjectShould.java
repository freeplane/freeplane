package org.freeplane.core.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConstantObjectShould {
	enum Rules {RULE}
	@Test
	public void returnItsObject() throws Exception {
		 ObjectRule <String, Rules> objectReference = new ConstantObject<String, Rules>("string");
		 assertThat(objectReference.getValue(), equalTo("string"));
	}

	@Test
	public void containItsObject() throws Exception {
		 ObjectRule <String, Rules> objectReference = new ConstantObject<String, Rules>("string");
		 assertThat(objectReference.hasValue(), equalTo(true));
	}

	@Test
	public void ignoreReset() throws Exception {
		 ObjectRule <String, Rules> objectReference = new ConstantObject<String, Rules>("string");
		 objectReference.resetCache();
		 assertThat(objectReference.hasValue(), equalTo(true));
	}
}
