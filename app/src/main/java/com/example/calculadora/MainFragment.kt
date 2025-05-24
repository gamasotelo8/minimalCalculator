package com.example.calculadora

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculadora.databinding.FragmentMainBinding
import de.congrace.exp4j.Calculable
import de.congrace.exp4j.ExpressionBuilder
import de.congrace.exp4j.UnknownFunctionException
import de.congrace.exp4j.UnparsableExpressionException


class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicklisteners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupClicklisteners() {

        // Números
        binding.btnOne.setOnClickListener { appendInput("1") }
        binding.btnTwo.setOnClickListener { appendInput("2") }
        binding.btnThree.setOnClickListener { appendInput("3") }
        binding.btnFour.setOnClickListener { appendInput("4") }
        binding.btnFive.setOnClickListener { appendInput("5") }
        binding.btnSix.setOnClickListener { appendInput("6") }
        binding.btnSeven.setOnClickListener { appendInput("7") }
        binding.btnEight.setOnClickListener { appendInput("8") }
        binding.btnNine.setOnClickListener { appendInput("9") }
        binding.btnZero.setOnClickListener { appendInput("0") }

        // Punto decimal
        binding.btnDot.setOnClickListener { appendDot() }

        // Operadores
        binding.btnAddition.setOnClickListener { appendOperator("+") }
        binding.btnSubtraction.setOnClickListener { appendOperator("-") }
        binding.btnMultiplication.setOnClickListener { appendOperator("*") }
        binding.btnDivision.setOnClickListener { appendOperator("/") }

        binding.btnFirstParenthesis.setOnClickListener { handleParenthesis("(") }
        binding.btnSecondParentesis.setOnClickListener { handleParenthesis(")") }

        // Botones especiales
        binding.btnAc.setOnClickListener { clearAll() }
        binding.btnDelete.setOnClickListener { deleteLastChar() }
        binding.btnEquals.setOnClickListener { handleEquals() }
    }

    private fun appendInput(value: String) {
        val currentText = binding.operations.text.toString()
        if(currentText.endsWith(")")){
            binding.operations.append("*$value")
        }else{
            binding.operations.append(value)
        }
        evaluateExpression()
    }

    private fun appendDot() {
        val currentText = binding.operations.text.toString()

        if (currentText.isEmpty() || currentText.last().isOperator() || currentText.last() == '(') {
            binding.operations.append("0.")
        } else {
            val lastNumberMatch = OPERATORS_REGEX.find(currentText.reversed())
            val lastNumber = if (lastNumberMatch != null) {
                currentText.reversed().substring(0, lastNumberMatch.range.last + 1)
            } else {
                currentText
            }

            if (!lastNumber.contains(".")) {
                binding.operations.append(".")
            }
        }
        evaluateExpression()
    }

    private fun appendOperator(operator: String) {
        val currentText = binding.operations.text.toString()

        if (currentText.isEmpty()) {
            if (operator == "-") {
                binding.operations.text = operator
            }
            binding.result.text = ""
            return
        }

        if (currentText.isNotEmpty() && currentText.last().isOperator()) {
            if(currentText.length > 1){
                binding.operations.text = currentText.dropLast(1) + operator
            }
        } else {
            binding.operations.append(operator)
        }
        binding.result.text = ""
    }

    private fun clearAll() {
        binding.operations.text = ""
        binding.result.text = ""
    }

    private fun deleteLastChar() {
        val currentText = binding.operations.text.toString()
        if (currentText.isNotEmpty()) {
            binding.operations.text = currentText.dropLast(1)
            evaluateExpression()
        } else {
            binding.result.text = ""
        }
    }

    private fun handleEquals() {
        val expressionText = binding.operations.text.toString()
        if (expressionText.isNotEmpty()) {
            try {
                val balancedExpression = balanceParentheses(expressionText)

                val expression = ExpressionBuilder(balancedExpression).build()
                val result = expression.calculate()
                displayFormattedResult(result, isFinalResult = true)
                binding.operations.text = binding.result.text
                binding.result.text = ""
            } catch (e: Exception) {
                binding.result.text = "Error"
                binding.operations.text = ""
            }
        }
    }

    private fun handleParenthesis(paren: String) {
        val currentText = binding.operations.text.toString()
        val openCount = currentText.count { it == '(' }
        val closeCount = currentText.count { it == ')' }

        if (paren == "(") {
            // Lógica para el paréntesis de apertura "("
            when {
                // Si la expresión está vacía, o termina en un operador, o ya en un paréntesis de apertura
                currentText.isEmpty() || (currentText.isNotEmpty() && currentText.last().isOperator()) || (currentText.isNotEmpty() && currentText.last() == '(') -> {
                    binding.operations.append("(")
                }
                // Si termina en un número, un paréntesis de cierre, o un punto
                // se asume multiplicación implícita antes de abrir un nuevo paréntesis.
                (currentText.isNotEmpty() && currentText.last().isDigit()) || (currentText.isNotEmpty() && currentText.last() == ')') || (currentText.isNotEmpty() && currentText.last() == '.') -> {
                    binding.operations.append("*(")
                }
                // Cualquier otro caso, simplemente añade el paréntesis de apertura
                else -> {
                    binding.operations.append("(")
                }
            }
        } else if (paren == ")") {
            // Lógica para el paréntesis de cierre ")"
            // 1. Debe haber al menos un paréntesis de apertura sin cerrar.
            // 2. La expresión no puede terminar en un operador, un paréntesis de apertura o un punto.
            if (openCount > closeCount &&
                currentText.isNotEmpty() &&
                !currentText.last().isOperator() && currentText.last() != '(' && currentText.last() != '.'
            ) {
                binding.operations.append(")")
            }
            // Si las condiciones no se cumplen, no se hace nada (comportamiento minimalista)
        }
        evaluateExpression() // Evalúa la expresión para el preview
    }

    private fun evaluateExpression() {
        val expressionText = binding.operations.text.toString()
        if (expressionText.isEmpty()) {
            binding.result.text = ""
            return
        }

        // No intentar evaluar si la expresión termina en un operador o un punto (expresión incompleta)
        if (expressionText.last().isOperator() || expressionText.last() == '(' || expressionText.last() == '.') {
            binding.result.text = ""
            return
        }

        try {
            // Intentar balancear paréntesis para el preview, si es posible
            val balancedExpression = balanceParentheses(expressionText)
            val expression = ExpressionBuilder(balancedExpression).build()
            val result = expression.calculate()
            displayFormattedResult(result, isFinalResult = false)
        } catch (e: Exception) {
            // No mostrar "Error" en el preview, simplemente limpiarlo
            binding.result.text = ""
        }
    }

    private fun displayFormattedResult(result: Double, isFinalResult: Boolean) {
        if (isWholeNumber(result)) {
            binding.result.text = result.toLong().toString()
        } else {
            // Formatear a un número razonable de decimales (ej. 8)
            // Eliminar ceros al final y el punto si no hay decimales restantes.
            val formatted = String.format("%.8f", result).trimEnd('0')
            binding.result.text = if (formatted.endsWith(".")) formatted.dropLast(1) else formatted
        }
    }

    private fun isWholeNumber(result: Double): Boolean {
        // Usar un pequeño épsilon para la comparación de punto flotante
        return Math.abs(result - result.toLong()) < 0.0000001
    }

    private fun Char.isOperator(): Boolean {
        return this == '+' || this == '-' || this == '*' || this == '/'
    }

    private val OPERATORS_REGEX = "[+\\-*/]".toRegex()


    private fun balanceParentheses(expression: String): String {
        var openCount = 0
        var closeCount = 0
        for (char in expression) {
            if (char == '(') openCount++
            else if (char == ')') closeCount++
        }
        val diff = openCount - closeCount
        return if (diff > 0) expression + ")".repeat(diff) else expression
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}