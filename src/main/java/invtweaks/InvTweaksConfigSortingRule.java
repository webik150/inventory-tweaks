package invtweaks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores a sorting rule, as a target plus a keyword. The target is provided as an array of preferred slots (ex: target
 * "1", i.e. first column, is stored as [0, 9, 18, 27])
 *
 * @author Jimeo Wan
 */
public class InvTweaksConfigSortingRule implements Comparable<InvTweaksConfigSortingRule> {
    private static final Pattern constraintVertical = Pattern.compile("v", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL);
    private static final Pattern constraintReverse = Pattern.compile("r", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL);
    private String constraint;
    @Nullable
    private int[] preferredPositions;
    private String keyword;
    private InvTweaksConfigSortingRuleType type;
    private int priority;
    private int containerSize;
    private int containerRowSize;

    public InvTweaksConfigSortingRule(@NotNull InvTweaksItemTree tree, String constraint_, String keyword_, int containerSize_, int containerRowSize_) {

        keyword = keyword_;
        constraint = constraint_;
        containerSize = containerSize_;
        containerRowSize = containerRowSize_;
        type = getRuleType(constraint, containerRowSize);
        preferredPositions = getRulePreferredPositions(constraint);

        // Compute priority
        // 1st criteria : the rule type
        // 2st criteria : the keyword category depth
        // 3st criteria : the item order in a same category

        priority = type.getLowestPriority() + 100000 + tree.getKeywordDepth(keyword) * 1000 - tree.getKeywordOrder(keyword);

    }

    @Nullable
    public static int[] getRulePreferredPositions(@NotNull String constraint, int containerSize, int containerRowSize) {

        @Nullable int[] result = null;
        int containerColumnSize = containerSize / containerRowSize;

        // Rectangle rules
        if(constraint.length() >= 5) {

            boolean vertical = false;
            @NotNull Matcher verticalMatcher = constraintVertical.matcher(constraint);
            if(verticalMatcher.find()) {
                vertical = true;
                constraint = verticalMatcher.reset().replaceAll("");
            }
            @NotNull String[] elements = constraint.split("-");
            if(elements.length == 2) {
                @Nullable int[] slots1 = getRulePreferredPositions(elements[0], containerSize, containerRowSize);
                @Nullable int[] slots2 = getRulePreferredPositions(elements[1], containerSize, containerRowSize);
                if(slots1.length == 1 && slots2.length == 1) {

                    int slot1 = slots1[0], slot2 = slots2[0];

                    @NotNull Point point1 = new Point(slot1 % containerRowSize, slot1 / containerRowSize), point2 = new Point(slot2 % containerRowSize, slot2 / containerRowSize);

                    result = new int[(Math.abs(point2.y - point1.y) + 1) * (Math.abs(point2.x - point1.x) + 1)];
                    int resultIndex = 0;

                    // Swap coordinates for vertical ordering
                    if(vertical) {
                        for(@NotNull Point p : new Point[]{point1, point2}) {
                            int buffer = p.x;
                            //noinspection SuspiciousNameCombination
                            p.x = p.y;
                            p.y = buffer;
                        }
                    }

                    int y = point1.y;
                    while((point1.y < point2.y) ? y <= point2.y : y >= point2.y) {
                        int x = point1.x;
                        while((point1.x < point2.x) ? x <= point2.x : x >= point2.x) {
                            result[resultIndex++] = (vertical) ? index(containerRowSize, x, y) : index(containerRowSize, y, x);
                            x += (point1.x < point2.x) ? 1 : -1;
                        }
                        y += (point1.y < point2.y) ? 1 : -1;
                    }

                    if(constraintReverse.matcher(constraint).find()) {
                        reverseArray(result);
                    }

                }
            }
        } else {

            // Default values
            int column = -1, row = -1;
            boolean reverse = false;

            // Extract chars
            for(int i = 0; i < constraint.length(); i++) {
                char c = constraint.charAt(i);
                int digitValue = Character.digit(c, 36); // radix-36 maps 0-9 to 0-9, and [a-zA-Z] to 10-36, see javadoc
                if(digitValue >= 1 && digitValue <= containerRowSize && digitValue < 10) {
                    // 1 column = 0, 9 column = 8
                    column = digitValue - 1;
                } else if(digitValue >= 10 && (digitValue - 10) <= containerColumnSize) {
                    // A row = 0, D row = 3, H row = 7
                    row = digitValue - 10;
                } else if(charEqualsIgnoreCase(c, 'r')) {
                    reverse = true;
                }
            }

            // Tile case
            if(column != -1 && row != -1) {
                result = new int[]{index(containerRowSize, row, column)};
            }
            // Row case
            else if(row != -1) {
                result = new int[containerRowSize];
                for(int i = 0; i < containerRowSize; i++) {
                    result[i] = index(containerRowSize, row, reverse ? containerRowSize - 1 - i : i);
                }
            }
            // Column case
            else {
                result = new int[containerColumnSize];
                for(int i = 0; i < containerColumnSize; i++) {
                    result[i] = index(containerRowSize, reverse ? i : containerColumnSize - 1 - i, column);
                }
            }
        }

        if(result == null) {
            InvTweaks.logInGameStatic("InvTweaks Config: Rule Constraint \"" + constraint + "\" was unable to be correctly determined.");
        }
        return result;
    }

    @NotNull
    public static InvTweaksConfigSortingRuleType getRuleType(@NotNull String constraint, int rowSize) {

        @NotNull InvTweaksConfigSortingRuleType result = InvTweaksConfigSortingRuleType.SLOT;

        if(constraint.length() == 1 || (constraint.length() == 2 && constraintReverse.matcher(constraint).find())) {
            constraint = constraintReverse.matcher(constraint).replaceAll("");
            // Column rule
            int digitValue = Character.digit(constraint.charAt(0), 10);
            if(digitValue >= 1 && digitValue <= rowSize) {
                result = InvTweaksConfigSortingRuleType.COLUMN;
            }
            // Row rule
            else {
                result = InvTweaksConfigSortingRuleType.ROW;
            }
        }
        // Rectangle rule
        else if(constraint.length() > 4) {
            // Special case: rectangle rule on a single column
            if(charEqualsIgnoreCase(constraint.charAt(1), constraint.charAt(4))) {
                result = InvTweaksConfigSortingRuleType.COLUMN;
            }
            // Special case: rectangle rule on a single row
            else if(charEqualsIgnoreCase(constraint.charAt(0), constraint.charAt(3))) {
                result = InvTweaksConfigSortingRuleType.ROW;
            }
            // Usual case
            else {
                result = InvTweaksConfigSortingRuleType.RECTANGLE;
            }
        }

        return result;

    }

    private static boolean charEqualsIgnoreCase(char a, char b) {
        // A crappy basic case-folding comparison, see String.equalsIgnoreCase & the behavior of Pattern with CASE_INSENSITIVE | UNICODE_CASE
        char aU = Character.toUpperCase(a), bU = Character.toUpperCase(b);
        return aU == bU || Character.toLowerCase(aU) == Character.toLowerCase(bU);
    }

    private static int index(int rowSize, int row, int column) {
        return row * rowSize + column;
    }

    private static void reverseArray(@NotNull int[] data) {
        int left = 0;
        int right = data.length - 1;
        while(left < right) {
            int temp = data[left];
            data[left] = data[right];
            data[right] = temp;
            left++;
            right--;
        }
    }

    public InvTweaksConfigSortingRuleType getType() {
        return type;
    }

    /**
     * @return An array of preferred positions (from the most to the less preferred).
     */
    @Nullable
    public int[] getPreferredSlots() {
        return preferredPositions;
    }

    public String getKeyword() {
        return keyword;
    }

    /**
     * @return rule priority (for rule sorting)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return Container size (TODO: Map rules to target section)
     */
    public int getContainerSize() {
        return containerSize;
    }

    /**
     * Compares rules priority : positive value means 'this' is of greater priority than o
     */
    public int compareTo(@NotNull InvTweaksConfigSortingRule o) {
        return priority - o.priority;
    }

    @Nullable
    public int[] getRulePreferredPositions(@NotNull String constraint) {
        // TODO Caching
        return InvTweaksConfigSortingRule.getRulePreferredPositions(constraint, containerSize, containerRowSize);
    }

    @NotNull
    public String toString() {
        return constraint + " " + keyword;
    }


}
