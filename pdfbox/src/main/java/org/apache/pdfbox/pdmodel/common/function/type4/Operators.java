/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.common.function.type4;

import java.util.Map;

/**
 * This class provides all the supported operators.
 * @version $Revision$
 */
public class Operators
{

    //Arithmetic operators
    private static final Operator ABS = new ArithmeticOperators.Abs();
    private static final Operator ADD = new ArithmeticOperators.Add();
    private static final Operator ATAN = new ArithmeticOperators.Atan();
    private static final Operator CEILING = new ArithmeticOperators.Ceiling();
    private static final Operator COS = new ArithmeticOperators.Cos();
    private static final Operator CVI = new ArithmeticOperators.Cvi();
    private static final Operator CVR = new ArithmeticOperators.Cvr();
    private static final Operator DIV = new ArithmeticOperators.Div();
    private static final Operator EXP = new ArithmeticOperators.Exp();
    private static final Operator FLOOR = new ArithmeticOperators.Floor();
    private static final Operator IDIV = new ArithmeticOperators.IDiv();
    private static final Operator LN = new ArithmeticOperators.Ln();
    private static final Operator LOG = new ArithmeticOperators.Log();
    private static final Operator MOD = new ArithmeticOperators.Mod();
    private static final Operator MUL = new ArithmeticOperators.Mul();
    private static final Operator NEG = new ArithmeticOperators.Neg();
    private static final Operator ROUND = new ArithmeticOperators.Round();
    private static final Operator SIN = new ArithmeticOperators.Sin();
    private static final Operator SQRT = new ArithmeticOperators.Sqrt();
    private static final Operator SUB = new ArithmeticOperators.Sub();
    private static final Operator TRUNCATE = new ArithmeticOperators.Truncate();

    //Relational, boolean and bitwise operators
    private static final Operator AND = new BitwiseOperators.And();
    private static final Operator BITSHIFT = new BitwiseOperators.Bitshift();
    private static final Operator EQ = new RelationalOperators.Eq();
    private static final Operator FALSE = new BitwiseOperators.False();
    private static final Operator GE = new RelationalOperators.Ge();
    private static final Operator GT = new RelationalOperators.Gt();
    private static final Operator LE = new RelationalOperators.Le();
    private static final Operator LT = new RelationalOperators.Lt();
    private static final Operator NE = new RelationalOperators.Ne();
    private static final Operator NOT = new BitwiseOperators.Not();
    private static final Operator OR = new BitwiseOperators.Or();
    private static final Operator TRUE = new BitwiseOperators.True();
    private static final Operator XOR = new BitwiseOperators.Xor();

    //Conditional operators
    private static final Operator IF = new ConditionalOperators.If();
    private static final Operator IFELSE = new ConditionalOperators.IfElse();

    //Stack operators
    private static final Operator COPY = new StackOperators.Copy();
    private static final Operator DUP = new StackOperators.Dup();
    private static final Operator EXCH = new StackOperators.Exch();
    private static final Operator INDEX = new StackOperators.Index();
    private static final Operator POP = new StackOperators.Pop();
    private static final Operator ROLL = new StackOperators.Roll();

    private Map<String, Operator> operators = new java.util.HashMap<String, Operator>();

    /**
     * Creates a new Operators object with the default set of operators.
     */
    public Operators()
    {
        operators.put("add", ADD);
        operators.put("abs", ABS);
        operators.put("atan", ATAN);
        operators.put("ceiling", CEILING);
        operators.put("cos", COS);
        operators.put("cvi", CVI);
        operators.put("cvr", CVR);
        operators.put("div", DIV);
        operators.put("exp", EXP);
        operators.put("floor", FLOOR);
        operators.put("idiv", IDIV);
        operators.put("ln", LN);
        operators.put("log", LOG);
        operators.put("mod", MOD);
        operators.put("mul", MUL);
        operators.put("neg", NEG);
        operators.put("round", ROUND);
        operators.put("sin", SIN);
        operators.put("sqrt", SQRT);
        operators.put("sub", SUB);
        operators.put("truncate", TRUNCATE);

        operators.put("and", AND);
        operators.put("bitshift", BITSHIFT);
        operators.put("eq", EQ);
        operators.put("false", FALSE);
        operators.put("ge", GE);
        operators.put("gt", GT);
        operators.put("le", LE);
        operators.put("lt", LT);
        operators.put("ne", NE);
        operators.put("not", NOT);
        operators.put("or", OR);
        operators.put("true", TRUE);
        operators.put("xor", XOR);

        operators.put("if", IF);
        operators.put("ifelse", IFELSE);

        operators.put("copy", COPY);
        operators.put("dup", DUP);
        operators.put("exch", EXCH);
        operators.put("index", INDEX);
        operators.put("pop", POP);
        operators.put("roll", ROLL);
    }

    /**
     * Returns the operator for the given operator name.
     * @param operatorName the operator name
     * @return the operator (or null if there's no such operator
     */
    public Operator getOperator(String operatorName)
    {
        return this.operators.get(operatorName);
    }

}
