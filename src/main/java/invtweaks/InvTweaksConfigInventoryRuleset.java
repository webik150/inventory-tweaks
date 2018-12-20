package invtweaks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Stores a whole configuration defined by rules. Several of them can be stored in the global configuration, as the mod
 * supports several rule configurations.
 *
 * @author Jimeo Wan
 */
public class InvTweaksConfigInventoryRuleset {
    private static final Pattern rulePattern = Pattern.compile("^(?:(?:[a-d1-9]r?)|(?:[a-d][1-9](?:-[a-d][1-9](?:rv?|vr?)?)?)) \\w+$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
    private String name;
    private int[] lockPriorities;
    private boolean[] frozenSlots;
    private List<InvTweaksConfigSortingRule> rules;
    private List<String> autoReplaceRules;
    private InvTweaksItemTree tree;

    /**
     * Creates a new configuration holder. The configuration is not yet loaded.
     */
    public InvTweaksConfigInventoryRuleset(InvTweaksItemTree tree_, @NotNull String name_) {
        tree = tree_;
        name = name_.trim();

        lockPriorities = new int[InvTweaksConst.INVENTORY_SIZE];
        for(int i = 0; i < lockPriorities.length; i++) {
            lockPriorities[i] = 0;
        }
        frozenSlots = new boolean[InvTweaksConst.INVENTORY_SIZE];
        for(int i = 0; i < frozenSlots.length; i++) {
            frozenSlots[i] = false;
        }

        rules = new ArrayList<>();
        autoReplaceRules = new ArrayList<>();
    }

    /**
     * @return If not null, returns the invalid keyword found
     * @throws InvalidParameterException
     */
    @Nullable
    public String registerLine(@NotNull String rawLine) throws InvalidParameterException {

        InvTweaksConfigSortingRule newRule;
        String lineText = rawLine.replaceAll("\\s+", " ");
        @NotNull String[] words = lineText.split(" ");

        // Parse valid lines only
        if(words.length == 2) {

            // Standard rules format
            if(rulePattern.matcher(lineText).matches()) {
                if(words[1].equalsIgnoreCase(InvTweaksConfig.LOCKED)) {
                    // Locking rule
                    @Nullable int[] newLockedSlots = InvTweaksConfigSortingRule.getRulePreferredPositions(words[0], InvTweaksConst.INVENTORY_SIZE, InvTweaksConst.INVENTORY_ROW_SIZE);
                    int lockPriority = InvTweaksConfigSortingRule.
                            getRuleType(words[0], InvTweaksConst.INVENTORY_ROW_SIZE).getLowestPriority() - 1;
                    for(int i : newLockedSlots) {
                        lockPriorities[i] = lockPriority;
                    }
                    return null;
                } else if(words[1].equalsIgnoreCase(InvTweaksConfig.FROZEN)) {

                    // Freeze rule
                    @Nullable int[] newLockedSlots = InvTweaksConfigSortingRule.getRulePreferredPositions(words[0], InvTweaksConst.INVENTORY_SIZE, InvTweaksConst.INVENTORY_ROW_SIZE);
                    for(int i : newLockedSlots) {
                        frozenSlots[i] = true;
                    }
                    return null;
                } else {
                    // Standard rule
                    String keyword = words[1];
                    boolean isValidKeyword = tree.isKeywordValid(keyword);

                    // If invalid keyword, guess something similar,
                    // but check first if it's not an item ID
                    // (can be used to make rules for unknown items)
                    // TODO: Should try looking up string ID.
                    /*if(!isValidKeyword) {
                        if(keyword.matches("^[0-9-]*$")) {
                            isValidKeyword = true;
                        } else {
                            List<String> wordVariants = getKeywordVariants(keyword);
                            for(String wordVariant : wordVariants) {
                                if(tree.isKeywordValid(wordVariant.toLowerCase())) {
                                    isValidKeyword = true;
                                    keyword = wordVariant;
                                    break;
                                }
                            }
                        }
                    }*/

                    if(isValidKeyword) {
                        newRule = new InvTweaksConfigSortingRule(tree, words[0], keyword, InvTweaksConst.INVENTORY_SIZE, InvTweaksConst.INVENTORY_ROW_SIZE);
                        rules.add(newRule);
                        return null;
                    } else {
                        return keyword;
                    }
                }
            } else if(words[0].equalsIgnoreCase(InvTweaksConfig.AUTOREFILL) || words[0].equalsIgnoreCase("autoreplace")) { // Compatibility
                // Autoreplace rule
                words[1] = words[1];
                if(tree.isKeywordValid(words[1]) || words[1].equalsIgnoreCase(InvTweaksConfig.AUTOREFILL_NOTHING)) {
                    autoReplaceRules.add(words[1]);
                }
                return null;
            }
        }

        throw new InvalidParameterException();

    }

    public void finalizeRules() {

        // Default Autoreplace behavior
        if(autoReplaceRules.isEmpty()) {
            try {
                autoReplaceRules.add(tree.getRootCategory().getName());
            } catch(NullPointerException e) {
                throw new NullPointerException("No root category is defined.");
            }
        }

        // Sort rules by priority, highest first
        rules.sort(Collections.reverseOrder());
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the lockPriorities
     */
    public int[] getLockPriorities() {
        return lockPriorities;
    }

    /**
     * @return the frozenSlots
     */
    public boolean[] getFrozenSlots() {
        return frozenSlots;
    }

    /**
     * @return the rules
     */
    public List<InvTweaksConfigSortingRule> getRules() {
        return rules;
    }

    /**
     * @return the autoReplaceRules
     */
    public List<String> getAutoReplaceRules() {
        return autoReplaceRules;
    }
}
