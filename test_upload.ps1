
$filePath = "test_upload.txt"
$uri = "http://localhost:8080/api/files/upload"

$fileStream = [System.IO.File]::OpenRead($filePath)
$multipartContent = [System.Net.Http.MultipartFormDataContent]::new()
$fileContent = [System.Net.Http.StreamContent]::new($fileStream)
$multipartContent.Add($fileContent, "file", "test_upload.txt")

$httpClient = [System.Net.Http.HttpClient]::new()
try {
    $response = $httpClient.PostAsync($uri, $multipartContent).GetAwaiter().GetResult()
    $responseContent = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Response: $responseContent"
} catch {
    Write-Host "Error: $_"
} finally {
    $fileStream.Close()
    $httpClient.Dispose()
}
