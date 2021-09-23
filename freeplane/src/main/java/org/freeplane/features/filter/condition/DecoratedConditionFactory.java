package org.freeplane.features.filter.condition;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class DecoratedConditionFactory {
    private static final String FILTER_FOR_SELF = "filter_for_self";
    private final Map<TranslatedObject, Function<ASelectableCondition, ASelectableCondition>> byDescription;
    private final Map<String, BiFunction<ConditionFactory, XMLElement, ASelectableCondition>> byName;
    Vector<TranslatedObject> keys;
    {
        byDescription = new LinkedHashMap<>();
        byDescription.put(TextUtils.createTranslatedString(FILTER_FOR_SELF), x -> x);
        byDescription.put(TextUtils.createTranslatedString(ParentConditionDecorator.DESCRIPTION), ParentConditionDecorator::new);
        byDescription.put(TextUtils.createTranslatedString(AnyAncestorConditionDecorator.DESCRIPTION), AnyAncestorConditionDecorator::new);
        byDescription.put(TextUtils.createTranslatedString(AnyChildConditionDecorator.DESCRIPTION), AnyChildConditionDecorator::new);
        byDescription.put(TextUtils.createTranslatedString(AllChildrenConditionDecorator.DESCRIPTION), AllChildrenConditionDecorator::new);
        byDescription.put(TextUtils.createTranslatedString(AnyDescendantConditionDecorator.DESCRIPTION), AnyDescendantConditionDecorator::new);
        byDescription.put(TextUtils.createTranslatedString(AllDescendantsConditionDecorator.DESCRIPTION), AllDescendantsConditionDecorator::new);
        
        keys = new Vector<>(byDescription.size());
        byDescription.keySet().forEach(keys::add);
        
        byName = new HashMap<>();
        byName.put(ConditionNotSatisfiedDecorator.NAME.toLowerCase(), ConditionNotSatisfiedDecorator::load);
        byName.put(ParentConditionDecorator.NAME.toLowerCase(), ParentConditionDecorator::load);
        byName.put(AnyAncestorConditionDecorator.NAME.toLowerCase(), AnyAncestorConditionDecorator::load);
        byName.put(AnyChildConditionDecorator.NAME.toLowerCase(), AnyChildConditionDecorator::load);
        byName.put(AllChildrenConditionDecorator.NAME.toLowerCase(), AllChildrenConditionDecorator::load);
        byName.put(AnyDescendantConditionDecorator.NAME.toLowerCase(), AnyDescendantConditionDecorator::load);
        byName.put(AllDescendantsConditionDecorator.NAME.toLowerCase(), AllDescendantsConditionDecorator::load);
    }
    
    public ASelectableCondition createRelativeCondition(TranslatedObject key, ASelectableCondition originalCondition) {
        return byDescription.get(key).apply(originalCondition);
    }
    
    public ASelectableCondition createRelativeCondition(ConditionFactory factory, XMLElement element) {
        BiFunction<ConditionFactory, XMLElement, ASelectableCondition> decoratorFactory = byName.get(element.getName().toLowerCase());
        return decoratorFactory == null ? null : decoratorFactory.apply(factory, element);
    }
    
    public Vector<TranslatedObject> getKeys(){
        return keys;
    }
}
