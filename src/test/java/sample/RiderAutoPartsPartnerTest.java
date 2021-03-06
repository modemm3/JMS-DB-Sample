/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample;

import java.sql.ResultSet;
import java.net.ConnectException;
import javax.sql.DataSource;
import java.util.Iterator;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @version $Revision: 131 $
 */
public class RiderAutoPartsPartnerTest extends CamelSpringTestSupport {

    private JdbcTemplate jdbc;

    @Before
    public void setupDatabase() throws Exception {
        DataSource ds = context.getRegistry().lookup("myDataSource", DataSource.class);
        jdbc = new JdbcTemplate(ds);

       java.util.List rs;
       rs = jdbc.queryForList("show tables like 'partner_metric'");


       Iterator it = rs.iterator();
       if (!it.hasNext()) {
            jdbc.execute("create table partner_metric "
                + "( partner_id varchar(10), time_occurred varchar(20), status_code varchar(3), perf_time varchar(10) )");
        }
    }

    @After
    public void dropDatabase() throws Exception {
        //jdbc.execute("drop table partner_metric");
    }

    @Test
    public void testSendPartnerReportIntoDatabase() throws Exception {
        // there should be 0 row in the database when we start
        jdbc.execute("delete from partner_metric");
        assertEquals(0, jdbc.queryForInt("select count(*) from partner_metric"));

        String xml = "<?xml version=\"1.0\"?><partner id=\"123\"><date>200911150815</date><code>200</code><time>4387</time></partner>";
        template.sendBody("activemq:queue:partnerRequestQueue?replyTo=partnerReplyQueue", xml);

        // wait for the route to complete (note we can use use mock to be notified when the route is complete)
        Thread.sleep(5000);

        // there should be 1 row in the database
        assertEquals(1, jdbc.queryForInt("select count(*) from partner_metric"));
    }


    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("sample/RiderAutoPartsPartnerTest.xml");
    }

}
