package com.eenact.dailymetrics.chart

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class ChartGeometryTest : FunSpec({

    context("yTicks") {
        test("run from zero to max inclusive") {
            yTicks(yMax = 8f, yStep = 2f).shouldContainExactly(0f, 2f, 4f, 6f, 8f)
        }

        test("include max despite float drift") {
            // 0.1 accumulates rounding error; the tolerance must still let the last tick through.
            val ticks = yTicks(yMax = 0.8f, yStep = 0.1f)
            ticks shouldHaveSize 9
            ticks.last() shouldBe (0.8f plusOrMinus 0.0001f)
        }

        test("stop before a max that is not a multiple of step") {
            yTicks(yMax = 7f, yStep = 3f).shouldContainExactly(0f, 3f, 6f)
        }

        context("do not loop forever with a non-positive step") {
            withData(nameFn = { "step $it" }, 0f, -1f, Float.NaN) { step ->
                yTicks(yMax = 5f, yStep = step).shouldContainExactly(0f)
            }
        }
    }

    context("formatTick") {
        withData(
            nameFn = { (value, text) -> "$value -> $text" },
            0f to "0",
            5f to "5",
            1000f to "1000",
            0.5f to "0.5",
            2.5f to "2.5",
            (0.1f + 0.2f) to "0.3",
            1.25f to "1.3",
        ) { (value, text) ->
            formatTick(value) shouldBe text
        }
    }

    context("PlotArea") {
        test("maps corners and midpoint of the data range") {
            val plot = PlotArea(left = 10f, top = 0f, right = 110f, bottom = 50f, xMin = 1_000L, xMax = 2_000L, yMax = 10f)

            plot.xOf(1_000L) shouldBe 10f
            plot.xOf(2_000L) shouldBe 110f
            plot.xOf(1_500L) shouldBe 60f

            plot.yOf(0f) shouldBe 50f
            plot.yOf(10f) shouldBe 0f
            plot.yOf(5f) shouldBe 25f
        }

        test("single instant does not divide by zero") {
            val plot = PlotArea(left = 0f, top = 0f, right = 100f, bottom = 100f, xMin = 500L, xMax = 500L, yMax = 10f)
            plot.xOf(500L) shouldBe 0f
        }

        test("zero yMax puts points on the baseline") {
            val plot = PlotArea(left = 0f, top = 0f, right = 100f, bottom = 80f, xMin = 0L, xMax = 10L, yMax = 0f)
            plot.yOf(0f) shouldBe 80f
        }
    }

    context("labelLeft") {
        test("centres the label on its position") {
            labelLeft(x = 50f, labelWidth = 20f, canvasWidth = 200f) shouldBe 40f
        }

        test("clamps the label inside the canvas") {
            labelLeft(x = 2f, labelWidth = 20f, canvasWidth = 200f) shouldBe 0f
            labelLeft(x = 199f, labelWidth = 20f, canvasWidth = 200f) shouldBe 180f
        }

        test("starts a label wider than the canvas at the left edge") {
            // Previously coerceIn(0, negative) threw IllegalArgumentException on very narrow canvases.
            labelLeft(x = 10f, labelWidth = 50f, canvasWidth = 30f) shouldBe 0f
        }
    }
})
