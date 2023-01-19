/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk;

import static java.util.Arrays.asList;
import static net.kaczmarzyk.utils.InterceptedStatementsAssert.assertThatInterceptedStatements;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.spring.data.jpa.Movie;
import net.kaczmarzyk.spring.data.jpa.MovieRepository;
import net.kaczmarzyk.spring.data.jpa.Person;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import net.kaczmarzyk.utils.interceptor.HibernateStatementInspector;


/**
 * @author Tomasz Kaczmarzyk
 */
public class AvoidingRedundantJoinE2eTest extends E2eTestBase {

    @Join(path = "stars", alias = "s", type = JoinType.LEFT)
    @Join(path = "directors", alias = "d", type = JoinType.LEFT)
	@And({
	        @Spec(path = "name", params = "name", spec = Like.class),
	        @Spec(path = "s.name", params = "star", spec = Like.class),
	        @Spec(path = "d.name", params = "director", spec = Like.class)
	})
	public static interface MovieSpec extends Specification<Movie> {
	}
	
	@RestController
	public static class MovieController {
		@Autowired
	    MovieRepository repository;

	    @GetMapping("/movies")
	    public Page<Movie> findMoviesByStarsAndDirectors(MovieSpec spec, @PageableDefault Pageable pageable) {
	        return repository.findAll(spec, pageable);
	    }
	}
	
	@Before
	public void persistTestData() {
		em.persist(new Movie("Bart's Not Dead",
				asList(new Person("Bart Simpson"), new Person("Homer Simpson")),
				asList(new Person("Bob Anderson"))));
		
		em.persist(new Movie("Heartbreak Hotel",
				asList(new Person("Marge Simpson"), new Person("Lisa Simpson")),
				asList(new Person("Steven Dean Moore"))));
		
		em.persist(new Movie("My Way or the Highway to Heaven",
				asList(new Person("Marge Simpson"), new Person("Homer Simpson")),
				asList(new Person("Rob Oliver"))));

		em.flush();

		HibernateStatementInspector.clearInterceptedStatements();
	}
	
	@Test
	public void performsEachJoinOnlyOnce() throws Exception {
		mockMvc.perform(get("/movies?star=Homer&director=Anderson"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(1));

		assertThatInterceptedStatements()
						.hasSelects(1)
						.hasNumberOfTableJoins("movie_stars", JoinType.LEFT, 1)
						.hasNumberOfTableJoins("movie_directors", JoinType.LEFT, 1);
	}
	
	@Test
	public void performsLeftJoinOnlyIfUsedInFiltering() throws Exception {
		mockMvc.perform(get("/movies?star=Homer"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(2));

		assertThatInterceptedStatements()
				.hasSelects(1)
				.hasNumberOfJoins(2)
				.hasNumberOfTableJoins("movie_stars", JoinType.LEFT, 1)
				.hasNumberOfTableJoins("movie_directors", JoinType.LEFT, 0);
	}
	
	@Test
	public void doesNotJoinAtAllIfFilteringNotAppliedOnLeftJoinPaths() throws Exception {
		mockMvc.perform(get("/movies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(3));

		assertThatInterceptedStatements()
				.hasSelects(1)
				.hasNumberOfJoins(0);
	}
		
}
