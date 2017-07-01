package org.apache.pdfbox.contentstream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class OneTimeArrayList<T> extends ArrayList<T> {
	
	private ArrayList<T> delegate;
	private boolean blowup = false;
	
	public OneTimeArrayList(ArrayList<T> delegate) {
		this.delegate = delegate;
	}
	
	public void setBlowup(boolean blowup) {
		this.blowup = blowup;
	}

	@Override
	public void trimToSize() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.trimToSize();
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.ensureCapacity(minCapacity);
	}

	@Override
	public int size() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.contains(o);
	}

	@Override
	public int indexOf(Object o) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.lastIndexOf(o);
	}

	@Override
	public Object clone() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.clone();
	}

	@Override
	public Object[] toArray() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.toArray(a);
	}

	@Override
	public T get(int index) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.get(index);
	}

	@Override
	public T set(int index, T element) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.set(index, element);
	}

	@Override
	public boolean add(T e) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.add(e);
	}

	@Override
	public void add(int index, T element) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.add(index, element);
	}

	@Override
	public T remove(int index) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.remove(o);
	}

	@Override
	public void clear() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.clear();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.retainAll(c);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.listIterator(index);
	}

	@Override
	public ListIterator<T> listIterator() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.listIterator();
	}

	@Override
	public Iterator<T> iterator() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.iterator();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.subList(fromIndex, toIndex);
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.forEach(action);
	}

	@Override
	public Spliterator<T> spliterator() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.spliterator();
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.removeIf(filter);
	}

	@Override
	public void replaceAll(UnaryOperator<T> operator) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.replaceAll(operator);
	}

	@Override
	public void sort(Comparator<? super T> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		delegate.sort(c);
	}

	@Override
	public boolean equals(Object o) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.hashCode();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.containsAll(c);
	}

	@Override
	public String toString() {
		if(blowup) throw new RuntimeException("Further interaction is blocked");
		return delegate.toString();
	}



	
	
}
