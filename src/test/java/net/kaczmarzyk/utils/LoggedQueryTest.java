/**
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.utils;

import jakarta.persistence.criteria.JoinType;
import org.junit.Test;

import static jakarta.persistence.criteria.JoinType.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Tomasz Kaczmarzyk
 */
public class LoggedQueryTest {

	@Test
	public void countsJoinsPerPath() {
		//Query from test: JoinAndJoinFetchReferringToTheSameTableTest#findsByOrdersAndName_join_and_join_fetch()
		LoggedQuery query = new LoggedQuery(
				"select distinct c1_0.id,c1_0.street,c1_0.birth_date,c1_0.date_of_next_special_offer,c1_0.date_of_next_special_offer_instant,c1_0.first_name,c1_0.gender,c1_0.gender_as_string,c1_0.gold,c1_0.gold_obj,c1_0.last_name,c1_0.last_order_time,c1_0.nick_name,c1_0.occupation,o1_0.customer_id,o1_0.id,o1_0.customer2_id,o1_0.item_name,o1_0.note_id,t1_0.order_id,t1_1.id,t1_1.name,c1_0.ref_code,c1_0.registration_date,c1_0.weight,c1_0.weight_big_decimal,c1_0.weight_double,c1_0.weight_float,c1_0.weight_int,c1_0.weight_long " +
						"from customer c1_0 " +
						"left join orders o1_0 on c1_0.id=o1_0.customer_id " +
						"left join (orders_tags t1_0 join item_tags t1_1 on t1_1.id=t1_0.tags_id) on o1_0.id=t1_0.order_id " +
						"left join orders o2_0 on c1_0.id=o2_0.customer_id " +
						"left join (orders_tags t2_0 join item_tags t2_1 on t2_1.id=t2_0.tags_id) on o2_0.id=t2_0.order_id " +
						"where 1=1 and 1=1 and upper(t2_1.name) like ? order by c1_0.id asc");

		assertThat(query.countTableJoins("orders")).isEqualTo(0);
		assertThat(query.countTableJoins("orders", LEFT)).isEqualTo(2);
		assertThat(query.countTableJoins("orders", INNER)).isEqualTo(0);
		assertThat(query.countTableJoins("orders", RIGHT)).isEqualTo(0);

		assertThat(query.countTableJoins("orders_tags")).isEqualTo(0);
		assertThat(query.countTableJoins("orders_tags", LEFT)).isEqualTo(2);
		assertThat(query.countTableJoins("orders_tags", INNER)).isEqualTo(0);
		assertThat(query.countTableJoins("orders_tags", RIGHT)).isEqualTo(0);


		assertThat(query.countTableJoins("item_tags")).isEqualTo(2);
		assertThat(query.countTableJoins("item_tags", LEFT)).isEqualTo(0);
		assertThat(query.countTableJoins("item_tags", INNER)).isEqualTo(2);
		assertThat(query.countTableJoins("item_tags", RIGHT)).isEqualTo(0);
	}
	
	@Test
	public void countsTotalNumberOfJoins() {
		LoggedQuery query = new LoggedQuery(
				"HQL: select distinct generatedAlias0 from net.kaczmarzyk.spring.data.jpa.Movie as generatedAlias0"
				+ " inner join generatedAlias0.stars as generatedAlias1"
				+ " inner join generatedAlias0.directors as generatedAlias2"
				+ " inner join generatedAlias0.stars as generatedAlias3"
				+ " inner join generatedAlias0.directors as generatedAlias4"
				+ " inner join generatedAlias0.producers as generatedAlias5");

		assertThat(query.countJoins()).isEqualTo(5);
	}
}
