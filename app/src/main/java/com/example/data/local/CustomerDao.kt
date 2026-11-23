package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
  @Transaction
  @Query("SELECT * FROM customers ORDER BY createdAt DESC")
  fun observeAllCustomers(): Flow<List<CustomerWithDetails>>

  @Query("SELECT COUNT(*) FROM customers")
  suspend fun countCustomers(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJobs(jobs: List<JobEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<PaymentEntity>)

  @Query("DELETE FROM jobs WHERE customerId = :customerId")
  suspend fun deleteJobsForCustomer(customerId: String)

  @Query("DELETE FROM customers WHERE id = :customerId")
  suspend fun deleteCustomerById(customerId: String)

  @Query("DELETE FROM customers WHERE isSample = 1")
  suspend fun deleteSampleCustomers()

  @Query("DELETE FROM customers")
  suspend fun deleteAllCustomers()

  @Query("UPDATE customers SET stage = :stageKey WHERE id = :customerId")
  suspend fun updateStage(customerId: String, stageKey: String)

  @Query("DELETE FROM payments WHERE id = :paymentId")
  suspend fun deletePaymentById(paymentId: String)

  @Transaction
  suspend fun upsertCustomerWithJobsAndPayments(customer: Customer) {
    insertCustomer(customer.toEntity())
    deleteJobsForCustomer(customer.id)
    insertJobs(customer.toJobEntities())
    if (customer.payments.isNotEmpty()) {
      insertPayments(customer.toPaymentEntities())
    }
  }

  @Transaction
  suspend fun replaceAllCustomers(customers: List<Customer>) {
    deleteAllCustomers()
    customers.forEach { customer ->
      insertCustomer(customer.toEntity())
      insertJobs(customer.toJobEntities())
      if (customer.payments.isNotEmpty()) {
        insertPayments(customer.toPaymentEntities())
      }
    }
  }
}
