package io.github.shijianjs.ktjs.duckdb.arrow

import js.objects.unsafeJso
import js.buffer.ArrayBufferLike
import js.typedarrays.Int32Array
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.js.unsafeCast

/**
 * Verifies the parts of the wrapper that only a real JS engine can confirm: that the declared
 * imports resolve to the upstream package, that the literal constants equal the upstream enum
 * values, and that a value written through the wrapper is read back by the library itself.
 */
class ArrowWrapperTest {
    // region enums

    @Test
    fun typeLiteralsMatchUpstream() {
        assertEquals(0.0, Types.NONE.unsafeCast<Double>())
        assertEquals(1.0, Types.Null.unsafeCast<Double>())
        assertEquals(2.0, Types.Int.unsafeCast<Double>())
        assertEquals(5.0, Types.Utf8.unsafeCast<Double>())
        assertEquals(12.0, Types.List.unsafeCast<Double>())
        assertEquals(17.0, Types.Map.unsafeCast<Double>())
        assertEquals(21.0, Types.LargeList.unsafeCast<Double>())
        assertEquals(24.0, Types.Utf8View.unsafeCast<Double>())
        assertEquals(-1.0, Types.Dictionary.unsafeCast<Double>())
        assertEquals(-2.0, Types.Int8.unsafeCast<Double>())
        assertEquals(-12.0, Types.Float64.unsafeCast<Double>())
        assertEquals(-23.0, Types.DenseUnion.unsafeCast<Double>())
        assertEquals(-31.0, Types.IntervalMonthDayNano.unsafeCast<Double>())
    }

    @Test
    fun bufferAndUnitLiteralsMatchUpstream() {
        assertEquals(0.0, BufferTypes.OFFSET.unsafeCast<Double>())
        assertEquals(3.0, BufferTypes.TYPE.unsafeCast<Double>())
        assertEquals(0.0, DateUnits.DAY.unsafeCast<Double>())
        assertEquals(1.0, DateUnits.MILLISECOND.unsafeCast<Double>())
        assertEquals(2.0, TimeUnits.MICROSECOND.unsafeCast<Double>())
        assertEquals(3.0, TimeUnits.NANOSECOND.unsafeCast<Double>())
        assertEquals(0.0, IntervalUnits.YEAR_MONTH.unsafeCast<Double>())
        assertEquals(2.0, IntervalUnits.MONTH_DAY_NANO.unsafeCast<Double>())
        assertEquals(0.0, Precisions.HALF.unsafeCast<Double>())
        assertEquals(2.0, Precisions.DOUBLE.unsafeCast<Double>())
        assertEquals(0.0, UnionModes.Sparse.unsafeCast<Double>())
        assertEquals(1.0, UnionModes.Dense.unsafeCast<Double>())
        assertEquals(4.0, MetadataVersions.V5.unsafeCast<Double>())
        assertEquals(3.0, MessageHeaders.RecordBatch.unsafeCast<Double>())
        assertEquals(1.0, CompressionTypes.ZSTD.unsafeCast<Double>())
    }

    // endregion

    // region data types

    @Test
    fun integerTypesReadTheirWidth() {
        val i32 = Int32()
        assertEquals(32.0, i32.bitWidth)
        assertTrue(i32.isSigned)
        // Upstream's Int_ reports the shared Type.Int for every width.\r
        assertEquals(Types.Int, i32.typeId)
        assertEquals("Int32", i32.toString())

        val u64 = Uint64()
        assertEquals(64.0, u64.bitWidth)
        assertEquals(false, u64.isSigned)
        assertEquals(Types.Int, u64.typeId)
    }

    @Test
    fun nestedTypesReadTheirChildren() {
        val list = ListType(field("item", Int32()))
        assertEquals(Types.List, list.typeId)
        assertEquals("Int32", list.valueType.toString())
        assertEquals("item", list.valueField.name)
        assertEquals("List<Int32>", list.toString())

        val struct = Struct(arrayOf(field("a", Int32()), field("b", Utf8())))
        assertEquals(2, struct.children?.size)
        assertEquals("b", struct.children?.get(1)?.name)
    }

    @Test
    fun temporalTypesExposeTheirUnit() {
        assertEquals(TimeUnits.MILLISECOND, TimestampMillisecond().unit)
        assertNull(TimestampMillisecond().timezone)
        assertEquals("UTC", TimestampNanosecond("UTC").timezone)
        assertEquals(DateUnits.DAY, DateDay().unit)
        assertEquals(TimeUnits.NANOSECOND, DurationNanosecond().unit)
    }

    @Test
    fun typeGuardsFollowTheTypeId() {
        assertTrue(Int8().isInt)
        assertTrue(Utf8().isUtf8)
        assertEquals(false, Utf8().isInt)
        assertTrue(Struct(arrayOf(field("a", Int32()))).isStruct)
    }

    // endregion

    // region schema

    @Test
    fun fieldAndSchemaExposeTheirMembers() {
        val a = field("a", Int32())
        val b = Field("b", Utf8(), true)

        assertEquals("a", a.name)
        assertEquals(false, a.nullable)
        assertEquals("b: Utf8", b.toString())

        val schema = Schema(arrayOf(a, b))
        assertEquals(2, schema.fields.size)
        assertEquals(2, schema.names.size)
        assertEquals("b", schema.names[1])
        assertEquals(0, schema.metadata.size)

        val selected = schema.select(arrayOf("b"))
        assertEquals(1, selected.fields.size)
        assertEquals("b", selected.fields[0].name)
    }

    // endregion

    // region data and vectors

    @Test
    fun makeDataAndVectorRoundTripValues() {
        val values = Int32Array<ArrayBufferLike>(4)
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[3] = 4

        val data = makeData(
            unsafeJso<DataProps> {
                type = Int32()
                length = 4.0
                this.data = values
            },
        )
        val vector = Vector<Double>(arrayOf(data))

        assertEquals(4.0, vector.length)
        assertEquals(Types.Int, vector.type.typeId)
        assertEquals(1.0, vector.get(0.0))
        assertEquals(4.0, vector.get(3.0))
        // `slice(offset, length)` starts at the offset, so the first value of this slice is 2.
        assertEquals(2.0, vector.slice(1.0, 3.0).get(0.0))
        // `VectorName` derives from the shared `Type.Int`, so the width is not part of the name.
        assertEquals("IntVector", vector.VectorName)
    }

    // endregion

    // region tables and IPC

    @Test
    fun tableFromArraysIsReadableAndSerializable() {
        val ids = Int32Array<ArrayBufferLike>(3)
        ids[0] = 10
        ids[1] = 20
        ids[2] = 30

        val table = tableFromArrays(
            unsafeJso<Columns> {
                id = ids
            },
        )

        assertEquals(1.0, table.numCols)
        assertEquals(3.0, table.numRows)
        assertEquals("id", table.schema.fields[0].name)
        assertEquals(20.0, table.getChild("id")?.get(1.0))
        assertTrue(table.toString().contains("\"id\": 10"))

        val bytes = tableToIPC(table)
        val restored = tableFromIPC(bytes)
        assertEquals(3.0, restored.numRows)
        assertEquals(20.0, restored.getChild("id")?.get(1.0))
    }

    @Test
    fun typeChecksRecognizeArrowValues() {
        val table = tableFromArrays(
            unsafeJso<Columns> {
                id = Int32Array<ArrayBufferLike>(1)
            },
        )
        assertTrue(isArrowTable(table))
        assertTrue(isArrowSchema(table.schema))
        assertTrue(isArrowField(table.schema.fields[0]))
        assertTrue(isArrowDataType(Int32()))
        assertEquals(false, isArrowTable(Int32()))
    }

    // endregion
}

/** Columns object used by [ArrowWrapperTest.tableFromArraysIsReadableAndSerializable]. */
external interface Columns {
    var id: Int32Array<ArrayBufferLike>
}
