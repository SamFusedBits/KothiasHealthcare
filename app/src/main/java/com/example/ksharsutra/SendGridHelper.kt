package com.example.ksharsutra

import com.sendgrid.*
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import java.io.IOException

object SendGridHelper {
    // SendGrid Helper class to send emails

    private const val SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY"
    private const val FROM_EMAIL = "your_email@example.com"

    @Throws(IOException::class)
    fun sendEmail(toEmail: String, subject: String, content: String) {
        val from = Email(FROM_EMAIL)
        val to = Email(toEmail)
        val content = Content("text/plain", content)
        val mail = Mail(from, subject, to, content)

        val sendGrid = SendGrid(SENDGRID_API_KEY)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sendGrid.api(request)
            println(response.statusCode)
            println(response.body)
            println(response.headers)
        } catch (ex: IOException) {
            throw ex
        }
    }
}
