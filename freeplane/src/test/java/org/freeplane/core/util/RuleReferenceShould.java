package org.freeplane.core.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RuleReferenceShould {
	enum Rules {RULE}
	@Test
	public void returnRule() throws Exception {
		 ObjectRule <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
		 assertThat(objectReference.getRule(), equalTo(Rules.RULE));
	}

	@Test
	public void containsNoObjectAfterInitialization() throws Exception {
		 ObjectRule <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
		 assertThat(objectReference.hasValue(), equalTo(false));
	}
	

	@Test
	public void returnSetObject() throws Exception {
		 ObjectRule <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
		 objectReference.setCache("string");
		 assertThat(objectReference.getValue(), equalTo("string"));
	}
	
	@Test
	public void containObjectAfterItIsSet() throws Exception {
		 ObjectRule <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
		 objectReference.setCache("string");
		 assertThat(objectReference.hasValue(), equalTo(true));
	}

	@Test
	public void removeValueAfterReset() throws Exception {
		 ObjectRule <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
		 objectReference.setCache("string");
		 objectReference.resetCache();
		 assertThat(objectReference.hasValue(), equalTo(false));
	}

//
//	@Test
//	public void ignoreReset() throws Exception {
//		 ObjectReference <String, Rules> objectReference = new RuleReference<String, Rules>(Rules.RULE);
//		 objectReference.reset();
//		 assertThat(objectReference.hasValue(), equalTo(true));
//	}
}
