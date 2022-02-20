package one.dedic.jmri.decodernext.jgoodies;

import com.jgoodies.binding.value.BindingConverter;
import static com.jgoodies.common.base.Preconditions.checkArgument;
import static com.jgoodies.common.base.Preconditions.checkNotNull;

import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Function;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.InputContextAware;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;


/**
 * JGoodies-like ConverterFactory, that makes APIs that accept BindingCoverters public.
 */
public final class ConverterFactory {

    private ConverterFactory() {
        // Overrides default constructor; prevents instantiation.
    }
    
    public static <S, T> BindingConverter<S, T> inverse(BindingConverter<T, S> orig) {
        return new BindingConverter<S, T>() {
            @Override
            public T targetValue(S arg0) {
                return orig.sourceValue(arg0);
            }

            @Override
            public S sourceValue(T arg0) {
                return orig.targetValue(arg0);
            }
        };
    }

    public static BindingConverter<Boolean, Boolean> booleanNegator() {
        return new BooleanNegator();
    }

    /**
     * Creates and returns a ValueModel that converts Booleans
     * to the associated of the two specified strings, and vice versa.
     * {@code null} is mapped to an empty string.
     * Ignores cases when setting a text.
     *
     * @param trueText      the text associated with {@code Boolean.TRUE}
     * @param falseText     the text associated with {@code Boolean.FALSE}
     *
     * @return a ValueModel that converts boolean to the associated text
     *
     * @throws NullPointerException if {@code booleanSource},
     *     {@code trueText}, {@code falseText} or {@code nullText} is {@code null}
     * @throws IllegalArgumentException if {@code trueText} equals {@code falseText}
     */
    public static BindingConverter<Boolean, String> booleanToStringConverter(
            String trueText,
            String falseText) {
        return new BooleanToStringConverter(trueText, falseText, null);
    }

    public static enum BooleanStyle {
        Boolean, State, Confirmation
    }
    
    @NbBundle.Messages({
        "VALUE_True_Boolean=True",
        "VALUE_False_Boolean=False",
        "VALUE_Null_Boolean=Undefined",
        "VALUE_True_State=Enabled",
        "VALUE_False_State=Disabled",
        "VALUE_Null_State=Unknown",
        "VALUE_True_Confirmation=Yes",
        "VALUE_False_Confirmation=No",
        "VALUE_Null_Confirmation=Undecided",
    })
    public static BindingConverter<Boolean, String> booleanToStringConverter(BooleanStyle style, boolean permitNull) {
        String t = NbBundle.getMessage(ConverterFactory.class, "VALUE_True_" + style.name());
        String f = NbBundle.getMessage(ConverterFactory.class, "VALUE_False_" + style.name());
        String n = permitNull ? NbBundle.getMessage(ConverterFactory.class, "VALUE_Null_" + style.name()) : null;
        return createBooleanToStringConverter(t, f, n);
    }

    /**
     * Creates and returns a ValueModel that converts Booleans
     * to the associated of the two specified strings, and vice versa.
     * {@code null} is mapped to the specified text.
     * Ignores cases when setting a text.
     *
     * @param trueText      the text associated with {@code Boolean.TRUE}
     * @param falseText     the text associated with {@code Boolean.FALSE}
     * @param nullText      the text associated with {@code null}
     *
     * @return a ValueModel that converts boolean to the associated text
     *
     * @throws NullPointerException if {@code booleanSource},
     *     {@code trueText}, {@code falseText} or {@code nullText} is {@code null}
     * @throws IllegalArgumentException if {@code trueText} equals {@code falseText}
     */
    public static BindingConverter<Boolean, String> createBooleanToStringConverter(
            String trueText,
            String falseText,
            String nullText) {
        return new BooleanToStringConverter(trueText, falseText, nullText);
    }
    
    public static BindingConverter<Integer, String> createStringToIntegerConverter() {
        return (BindingConverter<Integer, String>)(BindingConverter)new StringToIntConverter(
                NumberFormat.getIntegerInstance());
    }

    /**
     * Negates Booleans leaving null unchanged. Maps Boolean.TRUE
     * to Boolean.FALSE, Boolean.FALSE to Boolean.TRUE, and null to null.
     */
    public static final class BooleanNegator 
    	implements BindingConverter<Boolean, Boolean> {

        /**
         * Returns the negated source Boolean leaving null unchanged.
         */
        @Override
        public Boolean targetValue(Boolean sourceValue) {
            return negate(sourceValue);
        }


        /**
         * Returns the negated target Boolean leaving null unchanged.
         */
        @Override
		public Boolean sourceValue(Boolean targetValue) {
            return negate(targetValue);
        }


        /**
         * Negates Booleans leaving null unchanged.
         * Maps Boolean.TRUE to Boolean.FALSE ,
         * Boolean.FALSE to Boolean.TRUE, and null to null.
         *
         * @param value   the value to invert
         * @return the inverted Boolean value, or null if value is null
         */
        private static Boolean negate(Boolean value) {
            return value == null
            		? null
            		: Boolean.valueOf(!value.booleanValue());
        }

    }

    /**
     * Converts Booleans to Strings and vice-versa using given texts for
     * true, false, and null.
     */
    public static class BooleanToStringConverter 
    	implements BindingConverter<Boolean, String> {

        private final String trueText;
        private final String falseText;
        private final String nullText;

        protected BooleanToStringConverter(
                String trueText,
                String falseText,
                String nullText) {
            this.trueText  = checkNotNull(trueText, "The trueText must not be null.");
            this.falseText = checkNotNull(falseText, "The falseText must not be null.");
            this.nullText = nullText;
            checkArgument(!trueText.equals(falseText),
                    "The trueText and falseText must be different.");
        }


        /**
         * Returns the source value's associated text representation.
         */
        @Override
        public String targetValue(Boolean sourceValue) {
        	if (sourceValue == null) {
                return nullText;
            }
            return sourceValue.booleanValue()
                            ? trueText
                            : falseText;
        }


        /**
         * Converts the given String and sets the associated Boolean as
         * the subject's new value. In case the new value equals neither
         * this class' trueText, nor the falseText, nor the nullText,
         * an IllegalArgumentException is thrown.
         *
         * @throws IllegalArgumentException if the new value does neither match
         *     the trueText nor the falseText nor the nullText
         */
        @Override
		public Boolean sourceValue(String targetValue) {
            if (targetValue == null || "".equals(targetValue)) {
                if (nullText != null) {
                    return null;
                }
            } else {
                targetValue = targetValue.trim();
                if (trueText.equalsIgnoreCase(targetValue)) {
                    return Boolean.TRUE;
                } else if (falseText.equalsIgnoreCase(targetValue)) {
                    return Boolean.FALSE;
                } else if (nullText.equalsIgnoreCase(targetValue)) {
                    return null;
                }
            } 
            throw new IllegalArgumentException(
                    "The new value must be one of: "
                  + trueText + '/'
                  + falseText + '/'
                  + nullText);
        }

    }
    
    public static class StringToIntConverter extends StringConverter {
        public StringToIntConverter(Format format) {
            super(format);
        }

        public StringToIntConverter(Format format, Function<String, String> errMsg) {
            super(format, errMsg);
        }

        @Override
        public Object sourceValue(String targetValue) {
            Object o = super.sourceValue(targetValue);
            if (o instanceof Number) {
                return ((Number)o).intValue();
            } else {
                throw new IllegalArgumentException(targetValue);
            }
        }
        
    }

    /**
     * Converts Values to Strings and vice-versa using a given Format.
     */
    @NbBundle.Messages({
        "# {0} - the value text",
        "ERR_InvalidValueConversion=Invalid value: {0}"
    })
    public static class StringConverter 
    	implements BindingConverter<Object, String> {

        /**
         * Holds the {@code Format} used to format and parse.
         */
        private final Format format;

        private final Function<String, String> errMessageSupplier;

        // Instance Creation **************************************************

        /**
         * Constructs a {@code StringConverter} using the specified {@code Format}.
         *
         * @param format   the {@code Format} used to format and parse
         * @throws NullPointerException if the format is {@code null}.
         */
        public StringConverter(Format format) {
            this(format, (targetValue) -> Bundle.ERR_InvalidValueConversion(targetValue));
        }

        public StringConverter(Format format, Function<String, String> errMessageSupplier) {
            this.format = checkNotNull(format, "The format must not be null.");
            this.errMessageSupplier = errMessageSupplier;
        }


        // Implementing Abstract Behavior *************************************

        /**
         * Formats the source value and returns a String representation.
         *
         * @param sourceValue  the source value
         * @return the formatted sourceValue
         */
        @Override
        public String targetValue(Object sourceValue) {
            return format.format(sourceValue);
        }


        /**
         * Parses the given String encoding and returns the parsed object. 
         * {@code ParseException}s are re-thrown as IllegalArgumentException.
         *
         * @param targetValue  the value to be converted and set as new subject value
         */
        @Override
        public Object sourceValue(String targetValue) {
            try {
                return format.parseObject(targetValue);
            } catch (ParseException e) {
                throw new IllegalArgumentException(errMessageSupplier.apply(targetValue), e);
            }
        }

    }

    /**
     * Joins two binding converters into a chain, so it converts S &lt;-> T2. If at laest one of the
     * Converters provides Lookup.Provider interface, the result will also implement Lookup.Provider, delegating
     * first to b1, then to b2 converter.
     * @param <S>
     * @param <T1>
     * @param <T2>
     * @param b1
     * @param b2
     * @return 
     */
    public static <S, T1, T2> BindingConverter<S, T2> join(BindingConverter<S, T1> b1, BindingConverter<T1, T2> b2) {
        if (b1 instanceof Lookup.Provider || b2 instanceof Lookup.Provider) {
            return new ChainWithLookup<>(b1, b2);
        } else {
            return new ChainConverter<>(b1, b2);
        }
    }
    
    static class ChainConverter<S, T1, T2> implements BindingConverter<S, T2>, InputContextAware {
        final BindingConverter<S, T1> conv1;
        final BindingConverter<T1, T2> conv2;

        public ChainConverter(BindingConverter<S, T1> conv1, BindingConverter<T1, T2> conv2) {
            this.conv1 = conv1;
            this.conv2 = conv2;
        }

        @Override
        public T2 targetValue(S arg0) {
            return conv2.targetValue(conv1.targetValue(arg0));
        }

        @Override
        public S sourceValue(T2 arg0) {
            return conv1.sourceValue(conv2.sourceValue(arg0));
        }

        @Override
        public void useInputContext(InputContext ctx) {
            InputContext.callUseInputContext(ctx, conv1, conv2);
        }
    }
    
    static class ChainWithLookup<S, T1, T2> extends ChainConverter<S, T1, T2> implements Lookup.Provider {
        private Lookup lkp;

        public ChainWithLookup(BindingConverter<S, T1> conv1, BindingConverter<T1, T2> conv2) {
            super(conv1, conv2);
        }

        @Override
        public Lookup getLookup() {
            if (lkp != null) {
                return lkp;
            }
            synchronized (this) {
                if (lkp == null) {
                    if (conv1 instanceof Lookup.Provider) {
                        if (conv2 instanceof Lookup.Provider) {
                            lkp = new ProxyLookup(((Lookup.Provider)conv1).getLookup(), ((Lookup.Provider)conv2).getLookup());
                        } else {
                            lkp = ((Lookup.Provider)conv1).getLookup();
                        }
                    } else if (conv2 instanceof Lookup.Provider) {
                        lkp = ((Lookup.Provider)conv2).getLookup();
                    } else {
                        lkp = Lookup.EMPTY;
                    }
                }
                return lkp;
            }
        }
    }
}
