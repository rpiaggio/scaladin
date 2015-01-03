package vaadin.scala.tests

import com.vaadin.data.sort.SortOrder
import vaadin.scala.event.SelectionEvent
import com.vaadin.shared.data.sort.SortDirection
import com.vaadin.ui.Grid.{RowReference, Column}
import org.mockito.{ArgumentCaptor, Mockito}
import vaadin.scala.mixins.GridMixin
import vaadin.scala._
import vaadin.scala.Grid.HeightMode

/**
 *
 * @author Henri Kerola / Vaadin
 */
class GridTests extends ScaladinTestSuite {

  class VaadinGrid extends com.vaadin.ui.Grid with GridMixin {
    setContainerDataSource(new IndexedContainer().p)
    val f = classOf[com.vaadin.ui.Grid].getDeclaredField("defaultContainer")
    f.setAccessible(true)
    f.set(this, true)
  }

  var grid: Grid = _
  var vaadinGrid: VaadinGrid = _
  var spy: VaadinGrid = _

  before {
    vaadinGrid = new VaadinGrid
    spy = Mockito.spy(vaadinGrid)
    grid = new Grid(spy)
  }

  test("container") {
    assert(null != grid.container)

    val myContainer = new IndexedContainer
    grid.container = myContainer
    assert(myContainer == grid.container)
  }

  test("getColumn") {
    assert(grid.getColumn("fdf").isEmpty)

    Mockito.when(spy.getColumn("myid")).thenReturn(mock[Column])

    assert(grid.getColumn("myid").isDefined)
  }

  test("columns") {
    assert(grid.columns.isEmpty)

    grid.addColumn("col1")
    grid.addColumn("col2")

    assert(2 == grid.columns.size)
  }

  test("addColumn without type") {
    grid.addColumn("test")
    Mockito.verify(spy).addColumn("test")
  }

  test("addColumn with type") {
    grid.addColumn[String]("test")
    Mockito.verify(spy).addColumn("test", classOf[String])
  }

  test("removeAllColumns()") {
    grid.removeAllColumns()
    Mockito.verify(spy).removeAllColumns()
  }

  test("removeColumn") {
    grid.addColumn("propertyId")

    grid.removeColumn("propertyId")
    Mockito.verify(spy).removeColumn("propertyId")
  }



  // TODO tests for Grid.Column class

  test("setColumnOrder") {
    grid.addColumn("propertyId1")
    grid.addColumn("propertyId2")

    grid.setColumnOrder(Seq("propertyId1", "propertyId2"))

    Mockito.verify(spy).setColumnOrder("propertyId1", "propertyId2")
  }

  test("frozenColumnCount") {
    grid.addColumn("col")

    assert(0 == grid.frozenColumnCount)

    grid.frozenColumnCount = 1
    assert(1 == grid.frozenColumnCount)
  }

  test("scrollTo") {
    val itemId = grid.addRow()

    grid.scrollTo(itemId)
    Mockito.verify(spy).scrollTo(itemId)
  }

  test("scrollTo w/ ScrollDestination") {
    val itemId = grid.addRow()

    grid.scrollTo(itemId, Grid.ScrollDestination.Middle)
    Mockito.verify(spy).scrollTo(itemId, com.vaadin.shared.ui.grid.ScrollDestination.MIDDLE)
  }

  test("scrollToStart") {
    grid.scrollToStart()

    Mockito.verify(spy).scrollToStart()
  }

  test("scrollToEnd") {
    grid.scrollToEnd()

    Mockito.verify(spy).scrollToEnd()
  }

  test("heightByRows") {
    assert(10.0 == grid.heightByRows)

    grid.heightByRows = 5.5
    assert(5.5 == grid.heightByRows)
  }

  test("heightMode") {
    assert(Grid.HeightMode.Css == grid.heightMode)

    grid.heightMode = HeightMode.Row
    assert(Grid.HeightMode.Row == grid.heightMode)
  }

  test("selectionMode / selectionModel") {
    assert(SelectionMode.Multi == grid.selectionMode)
    assert(grid.selectionModel.isInstanceOf[Grid.SelectionModel.Multi])

    grid.selectionMode = SelectionMode.None
    assert(SelectionMode.None == grid.selectionMode)
    assert(grid.selectionModel.isInstanceOf[Grid.SelectionModel.None])

    grid.selectionMode = SelectionMode.Multi
    assert(SelectionMode.Multi == grid.selectionMode)
    assert(grid.selectionModel.isInstanceOf[Grid.SelectionModel.Multi])

    grid.selectionMode = SelectionMode.Single
    assert(SelectionMode.Single == grid.selectionMode)
    assert(grid.selectionModel.isInstanceOf[Grid.SelectionModel.Single])

    grid.selectionMode = SelectionMode.MultiSimple
    assert(SelectionMode.Multi == grid.selectionMode)
    assert(grid.selectionModel.isInstanceOf[Grid.SelectionModel.Multi])
  }

  test("isSelected") {
    assert(!grid.isSelected("itemid"))

    Mockito.when(spy.isSelected("selectedItemId")).thenReturn(true)
    assert(grid.isSelected("selectedItemId"))
  }

  test("selectedRows") {
    assert(grid.selectedRows.isEmpty)

    val arrayList = new java.util.ArrayList[AnyRef];
    arrayList.add("itemId1")
    arrayList.add("itemId2")

    Mockito.when(spy.getSelectedRows).thenReturn(arrayList)

    assert(Seq("itemId1", "itemId2") == grid.selectedRows)
  }

  test("selectedRow") {
    grid.selectionMode = SelectionMode.Single

    assert(grid.selectedRow.isEmpty)

    Mockito.verify(spy).getSelectedRow
  }

  test("select") {
    val itemId = grid.addRow()

    assert(grid.select(itemId))
    Mockito.verify(spy).select(itemId);
  }

  test("deselect") {
    Mockito.when(spy.deselect("itemId")).thenReturn(true)

    assert(grid.deselect("itemId"))

    Mockito.verify(spy).deselect("itemId");
  }

  ignore("selectionListeners") {
    grid.addColumn[String]("propertyId")
    val itemId = grid.addRow("value")
    var cnt = 0

    val selectionListener = { e: SelectionEvent =>
      cnt = cnt + 1
      assert(grid == e.component)
      assert(Seq(itemId) == e.added)
      assert(e.removed.isEmpty)
    }
    grid.selectionListeners += selectionListener

    grid.select(itemId)
    assert(1 == cnt)

    assert(1 == grid.selectionListeners.size)
    grid.selectionListeners -= selectionListener
    assert(grid.selectionListeners.isEmpty)
  }


  test("sortOrder") {
    import vaadin.scala.Grid.SortDirection._

    vaadinGrid.setContainerDataSource(new IndexedContainer().p)

    grid.container.addContainerProperty("propertyId1", classOf[String], None)
    grid.container.addContainerProperty("propertyId2", classOf[String], None)

    assert(grid.sortOrder.isEmpty)

    grid.sortOrder = Seq(("propertyId1", Ascending), ("propertyId2", Descending))

    assert(Seq(("propertyId1", Ascending), ("propertyId2", Descending)) == grid.sortOrder)

    val captor = ArgumentCaptor.forClass(classOf[java.util.List[SortOrder]])
    Mockito.verify(spy).setSortOrder(captor.capture())

    assert(SortDirection.ASCENDING == captor.getValue.get(0).getDirection)
    assert("propertyId1" == captor.getValue.get(0).getPropertyId)
    assert(SortDirection.DESCENDING == captor.getValue.get(1).getDirection)
    assert("propertyId2" == captor.getValue.get(1).getPropertyId)
  }

  test("sortOrder = Some") {
    import vaadin.scala.Grid.SortDirection._

    grid.container.addContainerProperty("propertyId1", classOf[String], None)
    grid.container.addContainerProperty("propertyId2", classOf[String], None)

    grid.sortOrder = Some(Seq(("propertyId1", Ascending), ("propertyId2", Descending)))

    assert(Seq(("propertyId1", Ascending), ("propertyId2", Descending)) == grid.sortOrder)

    val captor = ArgumentCaptor.forClass(classOf[java.util.List[SortOrder]])
    Mockito.verify(spy).setSortOrder(captor.capture())

    assert(SortDirection.ASCENDING == captor.getValue.get(0).getDirection)
    assert("propertyId1" == captor.getValue.get(0).getPropertyId)
    assert(SortDirection.DESCENDING == captor.getValue.get(1).getDirection)
    assert("propertyId2" == captor.getValue.get(1).getPropertyId)
  }

  test("sortOrder = None") {
    grid.sortOrder = None
    Mockito.verify(spy).clearSortOrder()
  }

  test("sortListeners") {
    grid.addColumn[String]("propertyId")
    var cnt = 0

    val sortListener = { e: Grid.SortEvent =>
      cnt = cnt + 1
      assert(grid == e.grid)
      assert(Seq(("propertyId", Grid.SortDirection.Ascending)) == e.sortOrder)
      assert(!e.userOriginated)
    }
    grid.sortListeners += sortListener

    grid.sort("propertyId", Grid.SortDirection.Ascending)
    assert(1 == cnt)

    assert(1 == grid.sortListeners.size)
    grid.sortListeners -= sortListener
    assert(0 == grid.sortListeners.size)
  }

  test("sort, ascending") {
    grid.addColumn[String]("propertyId")

    grid.sort("propertyId", Grid.SortDirection.Ascending)
    Mockito.verify(spy).sort("propertyId", com.vaadin.shared.data.sort.SortDirection.ASCENDING)
  }

  test("sort, descending") {
    grid.addColumn[String]("propertyId")

    grid.sort("propertyId", Grid.SortDirection.Descending)
    Mockito.verify(spy).sort("propertyId", com.vaadin.shared.data.sort.SortDirection.DESCENDING)
  }

  test("removeHeaderRow") {
    grid.removeHeaderRow(0)

    Mockito.verify(spy).removeHeaderRow(0)
  }

  test("defaultHeaderRow") {
    assert(grid.defaultHeaderRow.isDefined)

    grid.defaultHeaderRow = None
    assert(grid.defaultHeaderRow.isEmpty)

    // TODO
  }

  test("headerVisible") {
    assert(grid.headerVisible)

    grid.headerVisible = false
    assert(!grid.headerVisible)
  }


  test("getFooterRow") {
    grid.appendFooterRow()

    assert(null != grid.getFooterRow(0))
  }

  test("addFooterRowAt") {
    assert(null != grid.addFooterRowAt(0))

    Mockito.verify(spy).addFooterRowAt(0)
  }

  test("appendFooterRow") {
    assert(null != grid.appendFooterRow())

    Mockito.verify(spy).appendFooterRow()
  }

  test("footerRowCount") {
    assert(0 == grid.footerRowCount)

    Mockito.verify(spy).getFooterRowCount
  }

  test("prependFooterRow") {
    assert(null != grid.prependFooterRow())

    Mockito.verify(spy).prependFooterRow()
  }

  test("removeFooterRow") {
    grid.prependFooterRow()

    grid.removeFooterRow(0)

    Mockito.verify(spy).removeFooterRow(0)
  }

  test("footerVisible") {
    assert(grid.footerVisible)

    grid.footerVisible = false
    assert(!grid.footerVisible)
  }

  test("rowStyleGenerator") {
    assert(grid.rowStyleGenerator.isEmpty)

    grid.rowStyleGenerator = { e => None }
    assert(grid.rowStyleGenerator.isDefined)

    grid.rowStyleGenerator = None
    assert(grid.rowStyleGenerator.isEmpty)

    grid.rowStyleGenerator = Some({ e: Grid.RowReference => None})
    assert(grid.rowStyleGenerator.isDefined)
  }

  test("rowStyleGenerator generates correct stylename") {
    grid.rowStyleGenerator = { e => Some("123" + e.itemId) }

    val rr = new RowReference(grid.p)
    rr.set("myItemId")
    assert("123myItemId" == grid.p.getRowStyleGenerator.getStyle(rr))
  }

  test("cellStyleGenerator") {
    assert(grid.cellStyleGenerator.isEmpty)

    grid.cellStyleGenerator = { e => None }
    assert(grid.cellStyleGenerator.isDefined)

    grid.cellStyleGenerator = None
    assert(grid.cellStyleGenerator.isEmpty)

    grid.cellStyleGenerator = Some({ e: Grid.CellReference => None})
    assert(grid.cellStyleGenerator.isDefined)
  }

  test("addRow") {
    grid.addColumn("col1")
    grid.addColumn("col2")

    grid.addRow("a", "b")

    Mockito.verify(spy).addRow("a", "b")
  }

  test("editorEnabled") {
    grid.editorEnabled

    Mockito.verify(spy).isEditorEnabled
  }

  test("editedItemId") {
    assert(grid.editedItemId.isEmpty)

    Mockito.when(spy.getEditedItemId()).thenReturn("itemId", Nil: _*)

    assert(Some("itemId") == grid.editedItemId)
  }

  test("editorFieldGroup") {
    assert(null != grid.editorFieldGroup)

    val fieldGroup = new FieldGroup

    grid.editorFieldGroup = fieldGroup
    assert(fieldGroup == grid.editorFieldGroup)
  }

  test("editorRowActive") {
    Mockito.reset(spy)

    grid.editorActive

    Mockito.verify(spy).isEditorActive
  }

  ignore("getEditorRowField") {
    val myGrid = new Grid()

    myGrid.addColumn("propertyId")

    myGrid.getEditorRowField("propertyId")
  }

  test("editItem") {
    val itemId = grid.addRow()
    grid.editorEnabled = true

    grid.editItem(itemId)
  }

  test("saveEditor") {
    grid.saveEditor()

    Mockito.verify(spy).saveEditor()
  }

  test("cancelEditorRow") {
    grid.cancelEditor()

    Mockito.verify(spy).cancelEditor()
  }

}