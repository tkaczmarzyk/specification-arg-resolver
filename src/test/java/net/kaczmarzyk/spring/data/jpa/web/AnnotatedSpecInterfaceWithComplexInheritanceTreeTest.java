/**
 * Copyright 2014-2020 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web;

import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Conjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Disjunction;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.JoinType;
import java.util.Collection;

import static javax.persistence.criteria.JoinType.*;
import static net.kaczmarzyk.spring.data.jpa.web.utils.NativeWebRequestBuilder.nativeWebRequest;
import static org.assertj.core.api.Assertions.assertThat;


/**
 *  Inheritance tree of test case:
 *
 *  +--------------+    +--------------+    +--------------+
 *  | @Disjunction +---->   3 x @Join  |    |    @Joins    |
 *  +-----^--------+    +--------------+    +------^-------+
 *        |                                        |
 *  +-----+--------+                        +------+-------+       +--------------+
 *  |              |                        |              +-------> 3x@JoinFetch |
 *  +-----^--------+                        +------^-------+       +--------------+
 *        |                                        |
 *  +-----+--------+    +--------------+    +------+-------+
 *  |   @Spec      <----+   @Spec 2    |    |              |
 *  +-----^--------+    +------^-------+    +------^-------+
 *        |                    |                   |
 *        |                    |                   |
 *  +-----+--------+    +------+-------+   +-------+------+
 *  | @Conjunction |    |     @Or      |   | @JoinFetch   |
 *  +-------------^+    +------^-------+   +-------^------+
 *                |            |                   |
 *                |            |                   |
 *                |            |                   |
 *                |            |                   |
 *                +-----+------+-------+-----------+
 *                      |    @Join     |
 *                      +--------------+
 *
 * @author Jakub Radlica
 */
public class AnnotatedSpecInterfaceWithComplexInheritanceTreeTest extends AnnotatedSpecInterfaceTestBase {

	@Join(path = "repeatedJoin1", alias = "repeatedJoin1alias", type = LEFT, distinct = false)
	@Join(path = "repeatedJoin2", alias = "repeatedJoin2alias", type = INNER)
	@Join(path = "repeatedJoin3", alias = "repeatedJoin3alias", type = RIGHT, distinct = false)
	interface FilterWithRepeatedJoinAnnotations extends Specification<Object> {

	}

	@Disjunction(value = {
			@And(value = {
					@Spec(params = "disjunctionAnd1Param1", path = "disjunctionAnd1Path1", spec = Like.class),
					@Spec(params = "disjunctionAnd1Param2", path = "disjunctionAnd1Path2", spec = Equal.class)
			}),
			@And(value = {
					@Spec(params = "disjunctionAnd2Param1", path = "disjunctionAnd2Path1", spec = In.class),
					@Spec(params = "disjunctionAnd2Param2", path = "disjunctionAnd2Path2", spec = Between.class)
			})
	},
			or = {
					@Spec(params = "disjunctionOr1Param1", path = "disjunctionOr1Path1", spec = NotLike.class),
					@Spec(params = "disjunctionOr1Param2", path = "disjunctionOr1Path2", spec = NotIn.class)
			})
	interface DisjunctionFilter extends FilterWithRepeatedJoinAnnotations {
	}

	@Joins(
			value = {
					@Join(path = "joins1join1", alias = "joins1join1alias", distinct = true, type = LEFT),
					@Join(path = "joins1join2", alias = "joins1join2alias", distinct = false, type = JoinType.RIGHT),
					@Join(path = "joins1join3", alias = "joins1join3alias", distinct = true)
			}
	)
	interface JoinsFilter extends Specification<Object> {
	}

	interface EmptyFilterExtendingDisjunctionFilter extends DisjunctionFilter {

	}

	@JoinFetch(paths = {"repeatedJoinFetch1Path1", "repeatedJoinFetch1Path2"}, joinType = LEFT, distinct = false)
	@JoinFetch(paths = {"repeatedJoinFetch2Path1"}, joinType = INNER)
	@JoinFetch(paths = {"repeatedJoinFetch3Path1", "repeatedJoinFetch3Path2"}, joinType = RIGHT, distinct = false)
	interface FilterWithRepeatedJoinFetchAnnotations extends Specification<Object> {

	}

	interface EmptyFilterExtendingJoinsFilterAndFilterWithRepeatedJoinFetch extends JoinsFilter, FilterWithRepeatedJoinFetchAnnotations {

	}

	@Spec(params = "spec1", path = "spec1", spec = Like.class)
	interface SpecFilter extends EmptyFilterExtendingDisjunctionFilter {

	}

	@Spec(params = "spec2", path = "spec2", spec = Like.class)
	interface Spec2Filter extends SpecFilter {
	}

	interface EmptyFilterExtendingEmptyFilterExtendingJoinsFilter
			extends EmptyFilterExtendingJoinsFilterAndFilterWithRepeatedJoinFetch {
	}

	@Conjunction(
			value = {
					@Or({
							@Spec(params = "conjunction1or1spec1", path = "conjunction1or1spec1", spec = EqualIgnoreCase.class),
							@Spec(params = "conjunction1or1spec2", path = "conjunction1or1spec2", spec = NotEqualIgnoreCase.class)
					}),
					@Or({
							@Spec(params = "conjunction1or2spec1", path = "conjunction1or2spec1", spec = EqualIgnoreCase.class),
							@Spec(params = "conjunction1or2spec2", path = "conjunction1or2spec2", spec = NotEqualIgnoreCase.class)
					})
			},
			and = {
					@Spec(params = "conjunction1AndSpec1", path = "conjunction1AndSpec1", spec = Like.class),
					@Spec(params = "conjunction1AndSpec2", path = "conjunction1AndSpec2", spec = Equal.class),
			}
	)
	interface ConjunctionFilter extends SpecFilter {
	}

	@Or({
			@Spec(params = "or1spec1", path = "or1spec1", spec = NotNull.class),
			@Spec(params = "or1spec2", path = "or1spec2", spec = Null.class)
	})
	interface OrFilter extends Spec2Filter {

	}

	@JoinFetch(
			paths = { "joinFetch1path1", "joinFetch1path2", "joinFetch1path3" },
			joinType = LEFT
	)
	interface JoinFetchFilter extends EmptyFilterExtendingEmptyFilterExtendingJoinsFilter {

	}

	@Join(path = "join1", alias = "join1alias", distinct = false, type = LEFT)
	interface JoinFilter extends ConjunctionFilter, OrFilter, JoinFetchFilter {
	}

	static class TestController {
		public void testMethod(JoinFilter joinFilter) {
		}
	}

	@Test
	public void createsConjunctionOutOfSpecsFromWholeInheritanceTree() throws Exception {
		MethodParameter param = methodParameter("testMethod", JoinFilter.class);

		NativeWebRequest req = nativeWebRequest()
				.withParameterValues("disjunctionAnd1Param1", "disjunctionAnd1Param1Val")
				.withParameterValues("disjunctionAnd1Param2", "disjunctionAnd1Param2Val")
				.withParameterValues("disjunctionAnd2Param1", "disjunctionAnd2Param1Val")
				.withParameterValues("disjunctionAnd2Param2", "disjunctionAnd2Param2Val1", "disjunctionAnd2Param2Val2")
				.withParameterValues("disjunctionOr1Param1", "disjunctionOr1Param1Val")
				.withParameterValues("disjunctionOr1Param2", "disjunctionOr1Param2Val")
				.withParameterValues("spec1", "spec1Val")
				.withParameterValues("spec2", "spec2Val")
				.withParameterValues("conjunction1or1spec1", "conjunction1or1spec1Val")
				.withParameterValues("conjunction1or1spec2", "conjunction1or1spec2Val")
				.withParameterValues("conjunction1or2spec1", "conjunction1or2spec1Val")
				.withParameterValues("conjunction1or2spec2", "conjunction1or2spec2Val")
				.withParameterValues("conjunction1AndSpec1", "conjunction1AndSpec1Val")
				.withParameterValues("conjunction1AndSpec2", "conjunction1AndSpec2Val")
				.withParameterValues("or1spec1", "or1spec1Val")
				.withParameterValues("or1spec2", "or1spec2Val")
				.build();

		WebRequestProcessingContext ctx = new WebRequestProcessingContext(param, req);

		Specification<?> resolved = (Specification<?>) specificationArgumentResolver.resolveArgument(param, null, req, null);

		assertThat(resolved)
				.isInstanceOf(JoinFilter.class);

		Collection<Specification<Object>> resolvedInnerSpecs = innerSpecs(resolved);
		assertThat(resolvedInnerSpecs)
				.hasSize(10)
				.containsOnly(
						// DisjunctionFilter
						new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
										new Like<>(ctx.queryContext(), "disjunctionAnd1Path1", "disjunctionAnd1Param1Val"),
										new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "disjunctionAnd1Path2", new String[]{ "disjunctionAnd1Param2Val" }, converter))
								),
								new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
										new EmptyResultOnTypeMismatch<>(new In<>(ctx.queryContext(), "disjunctionAnd2Path1", new String[]{ "disjunctionAnd2Param1Val" }, converter)),
										new EmptyResultOnTypeMismatch<>(new Between<>(ctx.queryContext(), "disjunctionAnd2Path2", new String[]{ "disjunctionAnd2Param2Val1", "disjunctionAnd2Param2Val2" }, converter))
								),
								new NotLike<>(ctx.queryContext(), "disjunctionOr1Path1", "disjunctionOr1Param1Val"),
								new EmptyResultOnTypeMismatch<>(new NotIn<>(ctx.queryContext(), "disjunctionOr1Path2", new String[]{ "disjunctionOr1Param2Val" }, converter))
						),
						// JoinsFilter
						new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "joins1join1", "joins1join1alias", LEFT, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "joins1join2", "joins1join2alias", RIGHT, false),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "joins1join3", "joins1join3alias", INNER, true)
						),
						// SpecFilter
						new Like<>(ctx.queryContext(), "spec1", "spec1Val"),
						// Spec2Filter
						new Like<>(ctx.queryContext(), "spec2", "spec2Val"),
						// ConjunctionFilter
						new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
										new EmptyResultOnTypeMismatch<>(new EqualIgnoreCase<>(ctx.queryContext(), "conjunction1or1spec1", new String[]{ "conjunction1or1spec1Val" }, converter)),
										new EmptyResultOnTypeMismatch<>(new NotEqualIgnoreCase<>(ctx.queryContext(), "conjunction1or1spec2", new String[]{ "conjunction1or1spec2Val" }, converter))
								),
								new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
										new EmptyResultOnTypeMismatch<>(new EqualIgnoreCase<>(ctx.queryContext(), "conjunction1or2spec1", new String[]{ "conjunction1or2spec1Val" }, converter)),
										new EmptyResultOnTypeMismatch<>(new NotEqualIgnoreCase<>(ctx.queryContext(), "conjunction1or2spec2", new String[]{ "conjunction1or2spec2Val" }, converter))
								),
								new Like<>(ctx.queryContext(), "conjunction1AndSpec1", "conjunction1AndSpec1Val"),
								new EmptyResultOnTypeMismatch<>(new Equal<>(ctx.queryContext(), "conjunction1AndSpec2", new String[]{ "conjunction1AndSpec2Val" }, converter))
						),
						// OrFilter
						new net.kaczmarzyk.spring.data.jpa.domain.Disjunction<>(
								new EmptyResultOnTypeMismatch<>(new NotNull<>(ctx.queryContext(), "or1spec1", new String[]{ "or1spec1Val" }, converter)),
								new EmptyResultOnTypeMismatch<>(new Null<>(ctx.queryContext(), "or1spec2", new String[]{ "or1spec2Val" }, converter))
						),
						// JoinFetchFilter
						new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(ctx.queryContext(), new String[]{ "joinFetch1path1", "joinFetch1path2", "joinFetch1path3" }, LEFT, true),
						// JoinFilter
						new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "join1", "join1alias", LEFT, false),
						// 3xJoinFetch
						new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(ctx.queryContext(), new String[]{"repeatedJoinFetch1Path1", "repeatedJoinFetch1Path2"}, LEFT, false),
								new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(ctx.queryContext(), new String[]{"repeatedJoinFetch2Path1"}, INNER, true),
								new net.kaczmarzyk.spring.data.jpa.domain.JoinFetch<>(ctx.queryContext(), new String[]{"repeatedJoinFetch3Path1", "repeatedJoinFetch3Path2"}, RIGHT, false)
						),
						// 3xJoin
						new net.kaczmarzyk.spring.data.jpa.domain.Conjunction<>(
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "repeatedJoin1", "repeatedJoin1alias", LEFT, false),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "repeatedJoin2", "repeatedJoin2alias", INNER, true),
								new net.kaczmarzyk.spring.data.jpa.domain.Join<>(ctx.queryContext(), "repeatedJoin3", "repeatedJoin3alias", RIGHT, false)
						)
				);
	}

	@Override
	protected Class<?> controllerClass() {
		return TestController.class;
	}
}
