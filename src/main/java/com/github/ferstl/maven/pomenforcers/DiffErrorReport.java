package com.github.ferstl.maven.pomenforcers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.github.ferstl.maven.pomenforcers.model.PomSection;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


public class DiffErrorReport {

  public static void main(String[] args) {
    new DiffErrorReport().fuck();
  }



  public void fuck() {
//    List<String> actual = slightyDifferentOrder();
   List<String> actual = sortedOrder();
   List<String> required = requiredOrder();
   Patch<String> patch = DiffUtils.diff(actual, required);

   List<String> left = new ArrayList<String>(actual);
   List<String> right = new ArrayList<String>(actual);
   List<Delta<String>> deltas = patch.getDeltas();
   int offset = 0;
   for (Delta<String> delta : deltas) {
     Chunk<String> original = delta.getOriginal();
     Chunk<String> revised = delta.getRevised();

     System.out.println(delta + " " + revised.getPosition());
    switch(delta.getType()) {
       case INSERT:
         // Insert content on the right side, expand left side accordingly
         insertAll(right, offset + original.getPosition(), "+", revised.getLines());
         offset += insertEmptyLines(left, offset + original.getPosition(), revised.size());
         break;

       case CHANGE:
         int changeSize = Math.max(original.size(), revised.size());
         // left side: mark content as removed and fill in empty lines if the right side part of the delta is bigger
         markRemoved(left, offset + original.getPosition(), original.size());
         int nrOfInsertions = insertEmptyLines(left, offset + original.getPosition() + original.size(), changeSize - original.size());

         insertEmptyLines(right, offset + original.getPosition() + original.size(), nrOfInsertions);
         setLines(right, offset + original.getPosition(), "+", revised.getLines());
         offset += nrOfInsertions;
         clear(right, offset + original.getPosition() + revised.size(), changeSize - revised.size());

         break;
       case DELETE:
         markRemoved(left, offset + original.getPosition(), original.size());
         clear(right, offset + original.getPosition(), original.size()) ;
         break;

       default:
         throw new IllegalStateException("Unsupported delta type: " + delta.getType());

     }


   }

   System.out.println(sideBySide(left, right));
  }

  private int insertEmptyLines(List<String> l, int index, int nrOf) {
    if (nrOf < 1) {
      return 0;
    }

    String[] emptyLines = new String[nrOf];
    Arrays.fill(emptyLines, "");
    l.addAll(index, Arrays.asList(emptyLines));

    return emptyLines.length;
  }

  private void clear(List<String> l, int index, int nrOf) {
    if (nrOf < 1) {
      return;
    }

    for(int i = 0; i < nrOf; i++) {
      int insertionPoint = i + index;
      if (insertionPoint < l.size()) {
        l.set(i + index, "");
      } else {
        insertEmptyLines(l, insertionPoint, 1);
      }
    }
  }

  private void markRemoved(List<String> l, int index, int nrOf) {
    for (int i = index; i < index + nrOf; i++) {
      String value = l.get(i);
      l.set(i, "- " + value);
    }
  }

  private void setLines(List<String> l, int index, String prefix, Collection<String> lines) {
    int i = 0;

    for (String line : lines) {
      l.set(index + i++, prefix + " " + line);
    }
  }

  private void insertAll(List<String> l, int index, String prefix, Collection<String> content) {
    for (String string : content) {
      insert(l, index, prefix, string);
    }
  }

  private void insert(List<String> l, int index, String prefix, String content) {
    l.add(index, prefix + " " + content);
  }

  private String sideBySide(List<String> left, List<String> right) {
    int leftSize = left.size();
    int rightSize = right.size();
    int maxSize = Math.max(leftSize, rightSize);

    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < maxSize; i++) {
      String leftLine = i < leftSize ? left.get(i) : "";
      String rightLine = i < rightSize ? right.get(i) : "";

      String leftPadded = Strings.padEnd(leftLine, 25, ' ');
      String rightPadded = Strings.padEnd(rightLine, 25, ' ');

      sb.append(leftPadded)
      .append(" | ")
      .append(rightPadded)
      .append("\n");
    }

    // Remove last newline
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  private static List<String> requiredOrder() {
    return FluentIterable.from(Arrays.asList(PomSection.values()))
      .transform(new Function<PomSection, String>() {

        @Override
        public String apply(PomSection input) {
          return input.getSectionName();
        }})
      .toList();
  }

  private static List<String> sortedOrder() {
    return Ordering.usingToString().sortedCopy(requiredOrder());
  }

  private static List<String> slightyDifferentOrder() {
    List<String> list = new ArrayList<>(requiredOrder());
    swap(list, 0, 5);
    swap(list, 4, 10);

    return list;
  }

  private static void swap(List<String> list, int i1, int i2) {
    String tmp = list.get(i1);
    list.set(i1, list.get(i2));
    list.set(i2, tmp);
  }
}