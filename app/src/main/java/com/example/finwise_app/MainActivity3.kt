package com.example.finwise_app

import android.os.Bundle
import android.content.Intent
import android.app.Activity

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity3 : AppCompatActivity(),CategoryAdapter.OnItemClickListener  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = calculateGridLayoutManager()
        recyclerView.adapter = CategoryAdapter(getCategories(),this)
    }

    private fun getCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        categories.add(Category("Food", R.drawable.food_icon))
        categories.add(Category("Fuel", R.drawable.fuel_icon))
        categories.add(Category("Electric", R.drawable.ele_icon))
        categories.add(Category("Grocery", R.drawable.gros_icon))
        categories.add(Category("Fees", R.drawable.fees))
        categories.add(Category("Rent", R.drawable.rent_icon))
        categories.add(Category("Investment", R.drawable.invest_icon))
        categories.add(Category("Entertainment", R.drawable.entertainment))
        categories.add(Category("Pet", R.drawable.pet_icon))
        categories.add(Category("Fitness", R.drawable.fitness))
        categories.add(Category("Medical", R.drawable.medical))
        categories.add(Category("Loan", R.drawable.loan_icon))
        categories.add(Category("Appliance", R.drawable.home_appliance))
        return categories
    }

    private fun calculateGridLayoutManager(): GridLayoutManager {
        val numberOfColumns = calculateNumberOfColumns()
        return GridLayoutManager(this, numberOfColumns)
    }

    private fun calculateNumberOfColumns(): Int {
        val totalCategories = getCategories().size
        val desiredRows = 4 // Desired number of rows
        val itemsPerRow = totalCategories / desiredRows
        return if (itemsPerRow == 0) {
            1 // Ensure at least one column if there are fewer categories than desiredRows
        } else {
            itemsPerRow
        }
    }
    override fun onItemClick(category: Category) {
        val intent = Intent(this, expense_log::class.java)
        intent.putExtra("selectedCategoryImage", category.imageResource)
        intent.putExtra("selectedCategoryName", category.name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

