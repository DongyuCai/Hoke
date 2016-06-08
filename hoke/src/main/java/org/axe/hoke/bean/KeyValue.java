package org.axe.hoke.bean;

public class KeyValue<K,V> {

	private K key;
	private V value;
	
	public KeyValue(K key, V value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
}
