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
package net.kaczmarzyk.spring.data.jpa;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Movie {

	@Id
	@GeneratedValue
	private Long id;
	
	private String name;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable
	private List<Person> directors;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable
	private List<Person> stars;
	
	Movie() {
	}
	
	public Movie(String name, List<Person> stars, List<Person> directors) {
		this.name = name;
		this.stars = stars;
		this.directors = directors;
	}
	
	public String getName() {
		return name;
	}
}
