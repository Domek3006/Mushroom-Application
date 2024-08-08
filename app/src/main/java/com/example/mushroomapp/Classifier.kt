package com.example.mushroomapp

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.math.exp
import kotlin.math.round

class Classifier {
    lateinit var model : Module

    constructor() {}

    constructor(modelPath: String) {
        model = LiteModuleLoader.load(modelPath)
    }

    fun preprocess(bitmap: Bitmap, size: Int) : Tensor {
        var bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
    }

    fun argMax(inputs: DoubleArray) : Int {
        var maxIndex = -1
        var maxvalue = -Double.MAX_VALUE

        for (i in 0 until inputs.size) {
            if (inputs.get(i) > maxvalue) {
                maxvalue = inputs.get(i)
                maxIndex = i
            }
        }
        return maxIndex
    }

    private fun softmax(input: Double, total: Double): Double {
        return exp(input) / total
    }

    private fun convertFloatsToDoubles(input: FloatArray): DoubleArray {
        val output = DoubleArray(input.size)
        for (i in input.indices) {
            output[i] = input[i].toDouble()
        }
        return output
    }

    fun predict(bitmap: Bitmap) : Pair<IntArray, DoubleArray> {
        var tensor : Tensor = preprocess(bitmap, 299)

        var inputs: IValue = IValue.from(tensor)
        var outputs: Tensor = model.forward(inputs).toTensor()
        var scores_float: FloatArray = outputs.dataAsFloatArray

        var scores: DoubleArray = convertFloatsToDoubles(scores_float)

        val total: Double = Arrays.stream(scores).map { a: Double -> exp(a) }.sum()

        val probabilities = Arrays.stream(scores).map { a: Double -> softmax(a, total) }.toArray()

       var mostProb = DoubleArray(5)
       var ids = IntArray(5)

        for (i in 0 until 5 ) {
            var classIndex: Int = argMax(probabilities)
            mostProb[i] = String.format("%.2f", probabilities[classIndex]*100).replace(",",".").toDouble()
            ids[i] = classIndex + 1
            probabilities[classIndex] = 0.0
        }

        return Pair(ids, mostProb)
    }
}