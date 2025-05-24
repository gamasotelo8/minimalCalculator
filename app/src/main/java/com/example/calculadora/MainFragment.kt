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
        setOnClicklisteners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnClicklisteners() {
        val numberButtons = mapOf(
            binding.btnOne to "1",
            binding.btnTwo to "2",
            binding.btnThree to "3",
            binding.btnFour to "4",
            binding.btnFive to "5",
            binding.btnSix to "6",
            binding.btnSeven to "7",
            binding.btnEight to "8",
            binding.btnNine to "9",
            binding.btnZero to "0",
        )

        numberButtons.forEach { (button, value) ->
            button.setOnClickListener {
                binding.operations.text = binding.operations.text.toString() + value
                evaluate(binding.operations.text.toString())

            }
        }

        binding.btnDot.setOnClickListener {
            binding.operations.text = binding.operations.text.toString() + "."
        }
        val operatorButtons = mapOf(
            binding.btnAddition to "+",
            binding.btnSubtraction to "-",
            binding.btnMultiplication to "*",
            binding.btnDivision to "/"
        )

        operatorButtons.forEach { (button, operator) ->
            button.setOnClickListener {
                valitadeInputOperators(operator)
            }
        }

        binding.btnAc.setOnClickListener {
            binding.result.text = "";
            binding.operations.text = "";
        }

        binding.btnDelete.setOnClickListener {
            val text = binding.operations.text.toString()
            if (text.isNotEmpty()) {
                binding.operations.text = text.substring(0, text.length - 1)
                evaluate(binding.operations.text.toString())
            }
        }

        binding.btnPercent.setOnClickListener {
            if (binding.operations.text.toString().isNotEmpty()) {

            }
        }

        binding.btnEquals.setOnClickListener {
            if (binding.result.text.toString().isNotEmpty()) {
                binding.operations.text = binding.result.text.toString()
                binding.result.text = ""
            }
        }
    }

    private fun validateInput(input: String): Boolean {
        val operators = listOf("+", "-", "*", "/")
        var lastChar = input.lastOrNull()?.toString()
        var isValid = true

        if (input.contains("+") ||
            input.contains("-") ||
            input.contains("*") ||
            input.contains("/")
        ) {
            isValid = true
        } else {
            isValid = false
        }

        if (lastChar in operators || lastChar == "." || lastChar == "" || lastChar == null) {
            isValid = false
        }

        return isValid
    }

    private fun valitadeInputOperators(operator: String) {
        val operators = listOf("+", "-", "*", "/")

        binding.result.text = ""
        if (binding.operations.text.toString().isEmpty()) {
            return
        } else if (binding.operations.text.toString().last().toString() in operators) {
            binding.operations.text = binding.operations.text.toString().dropLast(1)
            binding.operations.text = binding.operations.text.toString() + operator
        } else {
            binding.operations.text = binding.operations.text.toString() + operator
        }
    }

    private fun calculateResult(expresion: String) {
        var calc: Calculable? = null
        try {
            calc = ExpressionBuilder(expresion).build()
            val result = calc.calculate()
            if (isWholeNumber(result)) {
                binding.result.text = result.toInt().toString()
            } else {
                binding.result.text = result.toString()
            }

        } catch (e: UnknownFunctionException) {
            binding.result.text = e.message
        } catch (e: UnparsableExpressionException) {
            binding.result.text = ""
        }
    }

    private fun evaluate(expresion: String) {
        if (validateInput(expresion)) {
            calculateResult(expresion)
        } else {
            binding.result.text = ""
        }
    }

    private fun isWholeNumber(result: Double): Boolean {
        val remainder: Double = result % 1

        if (remainder == 0.0)
            return true
        else
            return false
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