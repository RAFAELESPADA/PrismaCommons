package com.br.gabrielsilva.prismamc.commons.core.data;

import java.util.List;

import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

public class Data {

	private Object data;
	private boolean update;
	
	public Data(Object data) {
		this.data = data;
	}
	
	public Groups getGrupo() {
		return Groups.getFromString(getString());
	}
	
	public void add() {
		add(1);
	}
	
	public void add(int quantia) {
		setValue(getInt() + quantia);
	}
	
	public void remove() {
		remove(1);
	}
	
	public void remove(int quantia) {
		int atual = getInt();
		if (atual - quantia < 0) {
			setValue(0);
			return;
		}
		setValue(atual - quantia);
	}
	
	public void setValue(Object data) {
		this.data = data;
	}

	public Object getObject() {
		return data;
	}

	public String getString() {
		return (String) data;
	}

	public Integer getInt() {
		return (Integer) data;
	}

	public Long getLong() {
		return (Long) data;
	}

	public Boolean getBoolean() {
		return (Boolean) data;
	}

	public List<String> getList() {
		return (List<String>) data;
	}
	
	@Override
	public String toString() {
		if (data != null)
			return data.toString();
		return "null";
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}
}