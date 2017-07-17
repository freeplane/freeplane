package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import org.freeplane.plugin.collaboration.client.UpdateBean.ContentType;
import org.junit.Test;

import com.fasterxml.jackson.jr.ob.JSON;


public class UpdateBeanShould {
    protected String aposToQuotes(String json) {
        return json.replace("'", "\"");
    }
    
   @Test
   public void testSimpleBean() throws Exception
    {
        StringWriter w = new StringWriter();
		UpdateBean uut = new UpdateBean();
		uut.setNodeId("id");
		uut.setContentType(ContentType.TEXT);
		uut.setContent("content");
		JSON.std.write(uut, w);
		
        UpdateBean bean = JSON.std.beanFrom(UpdateBean.class, w.toString());

        assertThat(bean).isEqualTo(uut);
    }
}
