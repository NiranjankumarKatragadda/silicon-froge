package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.CustomSiliconRepository
import com.example.data.local.VlsiDatabase

class CustomSiliconViewModel(
  application: Application,
  val repository: CustomSiliconRepository = CustomSiliconRepository(
    codeDao = VlsiDatabase.getDatabase(application).studentCodeDao(),
    chipDao = VlsiDatabase.getDatabase(application).customChipDao()
  )
) : AndroidViewModel(application)
