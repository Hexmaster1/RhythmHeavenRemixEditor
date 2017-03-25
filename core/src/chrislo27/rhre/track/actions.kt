package chrislo27.rhre.track

import chrislo27.rhre.editor.SelectionGroup
import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.oopsies.ReversibleAction
import com.badlogic.gdx.math.Rectangle

class ActionAddEntities(entities: List<Entity>) : ReversibleAction<Remix> {

	constructor(vararg entities: Entity) : this(listOf(*entities))

	val entities = entities.toList()

	override fun redo(context: Remix) {
		context.entities.addAll(entities)
		context.updateDurationAndCurrentGame()
	}

	override fun undo(context: Remix) {
		context.entities.removeAll(entities)
		context.updateDurationAndCurrentGame()
	}

}

class ActionDeleteEntities(entities: List<Entity>) : ReversibleAction<Remix> {

	constructor(vararg entities: Entity) : this(listOf(*entities))

	private val entities = entities.toList()

	override fun redo(context: Remix) {
		context.entities.removeAll(entities)
		context.updateDurationAndCurrentGame()
	}

	override fun undo(context: Remix) {
		context.entities.addAll(entities)
		context.updateDurationAndCurrentGame()
	}

}

class ActionEditEntityBounds : ReversibleAction<Remix> {

	constructor(entities: List<Pair<Entity, Rectangle>>) {
		this.entities = entities.map {
			BoundsHistory(it.first, it.second)
		}.toList()
	}

	constructor(vararg entities: Pair<Entity, Rectangle>) : this(listOf(*entities))

	constructor(vararg entities: Triple<Entity, Rectangle, Rectangle>) {
		this.entities = entities.map { BoundsHistory(it.first, it.second, it.third) }
	}

	constructor(selectionGroup: SelectionGroup) {
		this.entities = selectionGroup.list.mapIndexed { index, entity ->
			BoundsHistory(entity, Rectangle().set(entity.bounds), Rectangle().set(selectionGroup.oldPositions[index]))
		}
	}

	private val entities: List<BoundsHistory>

	override fun redo(context: Remix) {
		entities.forEach { it.entity.bounds.set(it.newPos) }
		context.updateDurationAndCurrentGame()
	}

	override fun undo(context: Remix) {
		entities.forEach { it.entity.bounds.set(it.oldPos) }
		context.updateDurationAndCurrentGame()
	}

	data class BoundsHistory(val entity: Entity, val newPos: Rectangle,
							 val oldPos: Rectangle = Rectangle().set(entity.bounds))

}

class ActionSplitPattern(val pattern: PatternEntity) : ReversibleAction<Remix> {

	val splitResults = mutableListOf<Entity>()

	override fun redo(context: Remix) {
		context.entities.remove(pattern)
		pattern.internal.forEach { se ->
			val copy = se.copy()

			copy.bounds.x += pattern.bounds.x
			copy.bounds.y += pattern.bounds.y

			splitResults.add(copy)
		}

		context.entities.addAll(splitResults)

		context.updateDurationAndCurrentGame()
	}

	override fun undo(context: Remix) {
		context.entities.removeAll(splitResults)
		splitResults.clear()
		context.entities.add(pattern)
		context.updateDurationAndCurrentGame()
	}

}
