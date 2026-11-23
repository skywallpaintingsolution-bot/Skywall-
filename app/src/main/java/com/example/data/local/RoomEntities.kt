package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.JobItem
import com.example.data.model.PaymentItem

@Entity(tableName = "customers")
data class CustomerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val place: String,
  val stage: String,
  val discount: Double,
  val visitDate: String,
  val followDate: String,
  val notes: String,
  val isSample: Boolean,
  val createdAt: Long,
)

@Entity(
  tableName = "jobs",
  foreignKeys =
    [
      ForeignKey(
        entity = CustomerEntity::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("customerId")],
)
data class JobEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val customerId: String,
  val sortOrder: Int,
  val type: String,
  val colour: String,
  val hex: String,
  val area: Double,
  val rate: Double,
)

@Entity(
  tableName = "payments",
  foreignKeys =
    [
      ForeignKey(
        entity = CustomerEntity::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("customerId")],
)
data class PaymentEntity(
  @PrimaryKey val id: String,
  val customerId: String,
  val date: String,
  val amount: Double,
  val note: String,
)

data class CustomerWithDetails(
  @Embedded val customer: CustomerEntity,
  @Relation(parentColumn = "id", entityColumn = "customerId") val jobs: List<JobEntity>,
  @Relation(parentColumn = "id", entityColumn = "customerId") val payments: List<PaymentEntity>,
) {
  fun toDomain(): Customer =
    Customer(
      id = customer.id,
      name = customer.name,
      phone = customer.phone,
      place = customer.place,
      stage = CustomerStage.fromKey(customer.stage),
      jobs =
        jobs.sortedBy { it.sortOrder }.map {
          JobItem(
            id = it.id,
            type = it.type,
            colour = it.colour,
            hex = it.hex,
            area = it.area,
            rate = it.rate,
          )
        }.ifEmpty { listOf(JobItem()) },
      discount = customer.discount,
      visitDate = customer.visitDate,
      followDate = customer.followDate,
      notes = customer.notes,
      isSample = customer.isSample,
      createdAt = customer.createdAt,
      payments =
        payments.sortedByDescending { it.date }.map {
          PaymentItem(id = it.id, date = it.date, amount = it.amount, note = it.note)
        },
    )
}

fun Customer.toEntity(): CustomerEntity =
  CustomerEntity(
    id = id,
    name = name,
    phone = phone,
    place = place,
    stage = stage.key,
    discount = discount,
    visitDate = visitDate,
    followDate = followDate,
    notes = notes,
    isSample = isSample,
    createdAt = createdAt,
  )

fun Customer.toJobEntities(): List<JobEntity> =
  jobs.mapIndexed { index, job ->
    JobEntity(
      customerId = id,
      sortOrder = index,
      type = job.type,
      colour = job.colour,
      hex = job.hex,
      area = job.area,
      rate = job.rate,
    )
  }

fun Customer.toPaymentEntities(): List<PaymentEntity> =
  payments.map { payment ->
    PaymentEntity(
      id = payment.id,
      customerId = id,
      date = payment.date,
      amount = payment.amount,
      note = payment.note,
    )
  }
