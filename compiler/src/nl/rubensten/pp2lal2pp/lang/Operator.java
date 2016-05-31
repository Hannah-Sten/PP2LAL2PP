package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.IDManager;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;
import nl.rubensten.pp2lal2pp.compiler.Operand;

import java.util.Optional;

/**
 * @author Ruben Schellekens
 */
public enum Operator implements Identifyable, Operand {

    ADD_ASSIGN("+=", "\\+\\=", OperatorType.ASSIGNMENT, "ADD", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator += not supported on two numbers.");
    }),

    SUBSTRACT_ASSIGN("-=", "\\-\\=", OperatorType.ASSIGNMENT, "SUB", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator -= not supported on two numbers.");
    }),

    POWER_ASSIGN("**=", "\\*\\*\\=", OperatorType.ASSIGNMENT, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator **= not supported on two numbers.");
    }),

    MULTIPLY_ASSIGN("*=", "\\*\\=", OperatorType.ASSIGNMENT, "MULS", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator *= not supported on two numbers.");
    }),

    DIVIDE_ASSIGN("/=", "/\\=", OperatorType.ASSIGNMENT, "DIV", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator /= not supported on two numbers.");
    }),

    REMAINDER_ASSIGN("%=", "%\\=", OperatorType.ASSIGNMENT, "MOD", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator %= not supported on two numbers.");
    }),

    BITWISE_AND_ASSIGN("&=", "&\\=", OperatorType.ASSIGNMENT, "AND", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator &= not supported on two numbers.");
    }),

    BITWISE_OR_ASSIGN("|=", "\\|\\=", OperatorType.ASSIGNMENT, "OR", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator |= not supported on two numbers.");
    }),

    BITWISE_XOR_ASSIGN("^=", "\\^\\=", OperatorType.ASSIGNMENT, "XOR", (n1, n2) -> {
        throw new PP2LAL2PPException("Operator ^= not supported on two numbers.");
    }),

    ////

    ASSIGN_ALT_LEFT(":=", "\\:\\=", OperatorType.ASSIGNMENT, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator := not supported on two numbers.");
    }),

    ASSIGN_ALT_RIGHT("=:", "\\=\\:", OperatorType.ASSIGNMENT, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator =: not supported on two numbers.");
    }),

    ADDITION("+", "\\+", OperatorType.ARITHMETIC, "ADD", (n1, n2) -> {
        return new Number(n1.getIntValue() + n2.getIntValue());
    }),

    SUBSTRACTION("-", "\\-(?!\\d)", OperatorType.ARITHMETIC, "SUB", (n1, n2) -> {
        return new Number(n1.getIntValue() - n2.getIntValue());
    }),

    POWER("**", "\\*\\*", OperatorType.ARITHMETIC, null, (n1, n2) -> {
        return new Number((int)Math.pow(n1.getIntValue(), n2.getIntValue()));
    }),

    MULTIPLICATION("*", "\\*", OperatorType.ARITHMETIC, "MULS", (n1, n2) -> {
        return new Number(n1.getIntValue() * n2.getIntValue());
    }),

    INTEGER_DIVISION("/", "/", OperatorType.ARITHMETIC, "DIV", (n1, n2) -> {
        return new Number(n1.getIntValue() / n2.getIntValue());
    }),

    REMAINDER("%", "%", OperatorType.ARITHMETIC, "MOD", (n1, n2) -> {
        return new Number(n1.getIntValue() % n2.getIntValue());
    }),

    BITWISE_OR("|", "\\|", OperatorType.BITWISE, "OR", (n1, n2) -> {
        return new Number(n1.getIntValue() | n2.getIntValue());
    }),

    BITWISE_AND("&", "&", OperatorType.BITWISE, "AND", (n1, n2) -> {
        return new Number(n1.getIntValue() & n2.getIntValue());
    }),

    BITWISE_XOR("^", "\\^", OperatorType.BITWISE, "XOR", (n1, n2) -> {
        return new Number(n1.getIntValue() ^ n2.getIntValue());
    }),

    BITWISE_NOT("~", "~", OperatorType.BITWISE, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator ~ not supported on two numbers.");
    }),

    GREATHER_THAN_EQUAL(">=", "\\>\\=", OperatorType.RELATIONAL, "BGE", (n1, n2) -> {
        return (n1.getIntValue() >= n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    LESSER_THAN_EQUAL("<=", "\\<\\=", OperatorType.RELATIONAL, "BLE", (n1, n2) -> {
        return (n1.getIntValue() <= n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    EQUALS("==", "\\=\\=", OperatorType.RELATIONAL, "BEQ", (n1, n2) -> {
        return (n1.getIntValue() == n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    NOT_EQUALS("!=", "\\!\\=", OperatorType.RELATIONAL, "BNE", (n1, n2) -> {
        return (n1.getIntValue() != n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    GREATER_THAN(">", "\\>", OperatorType.RELATIONAL, "BGT", (n1, n2) -> {
        return (n1.getIntValue() > n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    LESSER_THAN("<", "\\<", OperatorType.RELATIONAL, "BLT", (n1, n2) -> {
        return (n1.getIntValue() < n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    ASSIGN("=", "\\=", OperatorType.ASSIGNMENT, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator = not supported on two numbers.");
    }),

    LOGICAL_OR(" or ", "( or )", OperatorType.LOGICAL, null, (n1, n2) -> {
        return (n1.getIntValue() == 1 || n2.getIntValue() == 1 ? Number.ONE : Number.ZERO);
    }),

    LOGICAL_AND(" and ", "( and )", OperatorType.LOGICAL, null, (n1, n2) -> {
        return (n1.getIntValue() == 1 && n2.getIntValue() == 1 ? Number.ONE : Number.ZERO);
    }),

    NUMBER_NEGATION("-", "\\-(?!\\d)", OperatorType.UNARY, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Unary operator - not supported on two numbers.");
    }) {
        @Override
        public Number calculateNumber(Number number) {
            return new Number(-number.getIntValue());
        }
    },

    BOOLEAN_NEGATION("!", "\\!", OperatorType.UNARY, null, (n1, n2) -> {
        throw new PP2LAL2PPException("Unary operator ! not supported on two numbers.");
    }) {
        @Override
        public Number calculateNumber(Number number) {
            if (number.getIntValue() == 0) {
                return Number.ONE;
            }
            else if (number.getIntValue() == 1) {
                return Number.ZERO;
            }
            else {
                return number;
            }
        }
    };

    /**
     * The unique ID of the operator.
     */
    private int id;

    /**
     * The string characterisation of the operator.
     */
    private String sign;

    /**
     * The escpaed character for regex. Yay.
     */
    private String regexSign;

    /**
     * The type of operator.
     */
    private OperatorType type;

    /**
     * The instruction that the operator executes.
     * <p>
     * Or <code>null</code> if there is none.
     */
    private String instruction;

    /**
     * Function to execute when executing the operator on two numbers.
     */
    private OperationFunction<Number> numbersFunction;

    Operator(String sign, String regexSign, OperatorType type, String instruction,
             OperationFunction<Number>
            numbersFunction) {
        this.id = IDManager.newId();
        this.sign = sign;
        this.regexSign = regexSign;
        this.type = type;
        this.instruction = instruction;
        this.numbersFunction = numbersFunction;
    }

    /**
     * Looks up the operator with the given sign.
     *
     * @param sign
     *         The sign to get the operator of.
     * @return The operator object corresponding to the given sign.
     */
    public static Optional<Operator> getBySign(String sign) {
        for (Operator op : values()) {
            if (op.getSign().equals(sign)) {
                return Optional.of(op);
            }
        }

        return Optional.empty();
    }

    /**
     * Apply the operator to two numbers.
     */
    public Number calculateNumbers(Number first, Number second) {
        return numbersFunction.operate(first, second);
    }

    /**
     * Apply the operator to one number.
     */
    public Number calculateNumber(Number number) {
        return number;
    }

    /**
     * @return The string characterisation of the operator.
     */
    public String getSign() {
        return sign;
    }

    public String getRegexSign() {
        return regexSign;
    }

    /**
     * @return The instruction name. E.g. LOAD, BNE.
     */
    public Optional<String> getInstruction() {
        if (instruction == null) {
            return Optional.empty();
        }

        return Optional.of(instruction);
    }

    /**
     * @return The type of operator.
     */
    public OperatorType getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return sign;
    }

    public enum OperatorType {
        ASSIGNMENT,
        ARITHMETIC,
        BITWISE,
        RELATIONAL,
        LOGICAL,
        UNARY;
    }

}
