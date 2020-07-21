package net.kaczmarzyk.spring.data.jpa.utils;

import javax.persistence.criteria.JoinType;

public class JoinFetchDefinition {

	private String alias;
	private String path;
	private JoinType joinType;

	public JoinFetchDefinition(String alias, String path, JoinType joinType) {
		this.alias = alias;
		this.path = path;
		this.joinType = joinType;
	}

	public String getAlias() {
		return alias;
	}

	public String getPath() {
		return path;
	}

	public JoinType getJoinType() {
		return joinType;
	}
}
