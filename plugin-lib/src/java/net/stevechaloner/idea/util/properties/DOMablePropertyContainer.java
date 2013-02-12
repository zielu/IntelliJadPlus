package net.stevechaloner.idea.util.properties;

import net.stevechaloner.idea.util.properties.converters.ConverterFactory;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steve Chaloner
 */
public class DOMablePropertyContainer extends AbstractDOMable implements PropertyContainer<PropertyDescriptor, DOMable>
{
    /**
     * Factories for creating DOMable instances.
     */
    private static final Map<DOMableType, TypeFactory> TYPE_FACTORIES = new HashMap<DOMableType, TypeFactory>()
    {
        {
            put(DOMableType.STRING,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        return new DOMableGeneric<String>(key,
                                                          ConverterFactory.getStringConverter(),
                                                          DOMableCollectionContentType.STRING);
                    }
                });
            put(DOMableType.BOOLEAN,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        return new DOMableGeneric<Boolean>(key,
                                                           ConverterFactory.getBooleanConverter(),
                                                           DOMableCollectionContentType.BOOLEAN);
                    }
                });
            put(DOMableType.INTEGER,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        return new DOMableGeneric<Integer>(key,
                                                           ConverterFactory.getIntegerConverter(),
                                                           DOMableCollectionContentType.INTEGER);
                    }
                });
            put(DOMableType.LIST,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        final DOMable value;
                        switch (DOMableCollectionContentType.getByName(e.getAttributeValue(DOMableCollectionContentType.CONTENT_TYPE)))
                        {
                            case BOOLEAN:
                                value = new DOMableList<Boolean>(key,
                                                                 ConverterFactory.getBooleanConverter());
                                break;
                            case INTEGER:
                                value = new DOMableList<Integer>(key,
                                                                 ConverterFactory.getIntegerConverter());
                                break;
                            case STRING:
                            default:
                                value = new DOMableList<String>(key,
                                                                ConverterFactory.getStringConverter());
                        }
                        return value;
                    }
                });
            put(DOMableType.MAP,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        final DOMable value;
                        switch (DOMableCollectionContentType.getByName(e.getAttributeValue(DOMableCollectionContentType.CONTENT_TYPE)))
                        {
                            case BOOLEAN:
                                value = new DOMableMap<Boolean>(key,
                                                                ConverterFactory.getBooleanConverter());
                                break;
                            case INTEGER:
                                value = new DOMableMap<Integer>(key,
                                                                ConverterFactory.getIntegerConverter());
                                break;
                            case STRING:
                            default:
                                value = new DOMableMap<String>(key,
                                                               ConverterFactory.getStringConverter());
                        }
                        return value;
                    }
                });
            put(DOMableType.SET,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        final DOMable value;
                        switch (DOMableCollectionContentType.getByName(e.getAttributeValue(DOMableCollectionContentType.CONTENT_TYPE)))
                        {
                            case BOOLEAN:
                                value = new DOMableSet<Boolean>(key,
                                                                ConverterFactory.getBooleanConverter());
                                break;
                            case INTEGER:
                                value = new DOMableSet<Integer>(key,
                                                                ConverterFactory.getIntegerConverter());
                                break;
                            case STRING:
                            default:
                                value = new DOMableSet<String>(key,
                                                               ConverterFactory.getStringConverter());
                        }

                        return value;
                    }
                });
            put(DOMableType.TABLE_MODEL,
                new TypeFactory()
                {
                    public DOMable create(Element e,
                                          PropertyDescriptor key)
                    {
                        return new DOMableTableModel(key,
                                                     createTableModel(e.getAttributeValue(DOMableTableModel.MODEL_CLASS)));
                    }

                    /**
                     * Reflectively creates an instance of the required model class.
                     *
                     * @param modelClass the model's class name
                     * @return an instance of a table model
                     */
                    private DefaultTableModel createTableModel(String modelClass)
                    {
                        DefaultTableModel model;
                        try
                        {
                            Class<?> modelClazz = Class.forName(modelClass);
                            model = (DefaultTableModel)modelClazz.newInstance();
                        }
                        catch (ClassNotFoundException e)
                        {
                            model = new DefaultTableModel();
                        }
                        catch (IllegalAccessException e)
                        {
                            model = new DefaultTableModel();
                        }
                        catch (InstantiationException e)
                        {
                            model = new DefaultTableModel();
                        }
                        return model;
                    }
                });
        }
    };

    /**
     * The values, mapped by the propety details.
     */
    private final Map<PropertyDescriptor, DOMable> values = new HashMap<PropertyDescriptor, DOMable>();

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor
     */
    public DOMablePropertyContainer(@NotNull PropertyDescriptor propertyDescriptor)
    {
        super(propertyDescriptor);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor
     * @param initialValues the initial values
     */
    public DOMablePropertyContainer(@NotNull PropertyDescriptor propertyDescriptor,
                                    @NotNull Map<PropertyDescriptor, DOMable> initialValues)
    {
        super(propertyDescriptor);
        values.putAll(initialValues);
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        for (PropertyDescriptor key : values.keySet())
        {
            Element e = element.getChild(key.getName());
            if (e != null)
            {
                String elementType = e.getAttributeValue(DOMableType.TYPE);
                TypeFactory typeFactory = TYPE_FACTORIES.get(DOMableType.getByName(elementType));
                DOMable value = typeFactory.create(e,
                                                   key);
                value.read(e);
                values.put(key,
                           value);
            }
        }
    }

    /** {@inheritDoc} */
    @NotNull
    public Element write()
    {
        Element parent = new Element(getPropertyDescriptor().getName());

        List<PropertyDescriptor> keys = new ArrayList<PropertyDescriptor>(values.keySet());
        Collections.sort(keys,
                         new Comparator<PropertyDescriptor>()
                         {
                             public int compare(PropertyDescriptor pd1,
                                                PropertyDescriptor pd2)
                             {
                                 return String.CASE_INSENSITIVE_ORDER.compare(pd1.getName(),
                                                                              pd2.getName());
                             }
                         });
        for (PropertyDescriptor key : keys)
        {
            DOMable value = values.get(key);
            parent.addContent(value.write());
        }

        return parent;
    }

    /** {@inheritDoc} */
    public DOMable get(@NotNull PropertyDescriptor key)
    {
        return values.get(key);
    }

    /** {@inheritDoc} */
    public void put(@NotNull PropertyDescriptor key,
                    @NotNull DOMable persistable)
    {
        values.put(key,
                   persistable);
    }

    /** {@inheritDoc} */
    public DOMable remove(@NotNull PropertyDescriptor key)
    {
        return values.remove(key);
    }

    /** {@inheritDoc} */
    @Nullable
    public Object getValue()
    {
        return null;
    }

    /**
     * Factory for DOMable types.
     */
    private static interface TypeFactory
    {
        /**
         * Create an instance of a DOMable type.
         *
         * @param element the element containing the description
         * @param key the key of the type
         * @return a DOMable type
         */
        DOMable create(Element element,
                       PropertyDescriptor key);
    }
}
