package com.yjz.microweb.util;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayUtils
{
 // Reworked the java.util.Arrays's binarySearch
    public static int binarySearch(final int[] a, final int fromIndex, final int toIndex,
                     final int key) {
    int low = fromIndex;
    int high = toIndex - 1;

    while (low <= high) {
        int mid = (low + high) >>> 1;
        int midVal = a[mid];

        if (midVal < key)
        low = mid + 1;
        else if (midVal > key)
        high = mid - 1;
        else
        return mid; // key found
    }
    return low;  // key not found.
    }

    /**
     * Add unique element to the array.
     * @param <T> type of the array element
     * @param array array
     * @param element element to add
     *
     * @return array, which will contain the new element. Either new array instance, if
     * passed array didn't contain the element, or the same array instance, if the element
     * is already present in the array.
     */
    public static <T> T[] addUnique(T[] array, T element) {
        return addUnique(array, element, true);
    }

    /**
     * Add unique element to the array.
     * @param <T> type of the array element
     * @param array array
     * @param element element to add
     * @param replaceElementIfEquals if passed element is equal to some element
     *              in the array then depending on this parameter it will be
     *              replaced or not with the passed element.
     *
     * @return array, which will contain the new element. Either new array instance, if
     * passed array didn't contain the element, or the same array instance, if the element
     * is already present in the array.
     */
    public static <T> T[] addUnique(final T[] array, final T element,
            final boolean replaceElementIfEquals) {
        
        final int idx = indexOf(array, element);
        if (idx == -1) {
            final int length = array.length;
            final T[] newArray = Arrays.copyOf(array, length + 1);
            newArray[length] = element;
            return newArray;
        }
        
        if (replaceElementIfEquals) {
            array[idx] = element;
        }
        
        return array;
    }

    /**
     * Removes the element from the array.
     * @param <T> type of the array element
     * @param array array
     * @param element the element to remove
     * 
     * @return array, which won't contain the element. Either new array instance, if
     * passed array contains the element, or the same array instance, if the element
     * wasn't present in the array. <tt>null</tt> will be returned if the last
     * element was removed from the passed array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(T[] array, Object element) {
        final int idx = indexOf(array, element);
        if (idx != -1) {
            final int length = array.length;

            if (length == 1) {
                return null;
            }

            final T[] newArray = (T[]) Array.newInstance(
                    array.getClass().getComponentType(), length - 1);
            
            if (idx > 0) {
                System.arraycopy(array, 0, newArray, 0, idx);
            }

            if (idx < length - 1) {
                System.arraycopy(array, idx + 1, newArray, idx, length - idx - 1);
            }

            return newArray;
        }

        return array;
    }

    /**
     * Return the element index in the array.
     * @param <T> type of the array element
     * @param array array
     * @param element the element to look for.
     *
     * @return element's index, or <tt>-1</tt> if element wasn't found.
     */
    public static <T> int indexOf(T[] array, Object element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }
}
